#!/bin/bash

set -e

OS="$(uname)"
arch="$(uname -m)"  # -i is only linux, -m is linux and apple
MY_OS="NOT_SET"
DOCKER_REGISTRY="selenium"
CHROME_REPO="node-chromium"
FIREFOX_REPO="node-firefox"
SELENIUM_HUB_REPO="hub"

case "$OS" in
  "Linux")
    echo "You are running Linux."
    MY_OS="linux"
    ip_address="$(ifconfig | grep -Eo 'inet (192|172|10)\.[0-9]+\.[0-9]+\.[0-9]+' | awk '{print $2}' | head -n 1)"
    ;;
  "Darwin")
    echo "You are running macOS."
    MY_OS="macOS"
    ip_address="$(ifconfig | grep -Eo 'inet (192|172|10)\.[0-9]+\.[0-9]+\.[0-9]+' | awk '{print $2}' | head -n 1)"
    ;;
  "CYGWIN"*|"MINGW"*|"MSYS"*)
    echo "You are running Windows (via Cygwin or Git Bash)."
    MY_OS="Windows"
    ip_address=$(ipconfig | grep "IPv4" | grep -E "192" | awk '{print $NF}' | head -n 1)
    ;;
  *)
    echo "Unknown OS: $OS"
    ;;
esac

echo "Running on OS: " $OS "-" $MY_OS
echo "Running on arch: $arch"
echo "Host IP Address: $ip_address"

if [[ "$ip_address" == "" ]]; then
  echo "IP address not found based on the criteria. Exit."
  ifconfig | grep "inet "
  exit 1
fi

export DOCKER_REGISTRY=$DOCKER_REGISTRY
export SELENIUM_HUB_IMAGE="$DOCKER_REGISTRY/${SELENIUM_HUB_REPO}:latest"
export CHROME_IMAGE="$DOCKER_REGISTRY/${CHROME_REPO}:latest"
export FIREFOX_IMAGE="$DOCKER_REGISTRY/${FIREFOX_REPO}:latest"
echo "Using the containers from DOCKER_REGISTRY: $DOCKER_REGISTRY for architecture: $arch"
echo "Selenium-hub image: ${SELENIUM_HUB_IMAGE}"
echo "Chrome image: ${CHROME_IMAGE}"
echo "Firefox image: ${FIREFOX_IMAGE}"

if [[ -z "${TESWIZ_GRID_PORT}" ]]; then
  echo "GRID_PORT is not set. Use default: 4444"
  GRID_PORT=4444
else
  GRID_PORT=$TESWIZ_GRID_PORT
fi

if [[ -z "${HTTP_PROXY}" ]]; then
  echo "HTTP_PROXY is not set."
  PROXY_KEY=NOT_SET
  DOCKER_COMPOSE_FILE_NAME="docker-compose-v3.yml"
else
  PROXY_KEY=$HTTP_PROXY
  DOCKER_COMPOSE_FILE_NAME="docker-compose-v3-proxy.yml"
fi
((GRID_PORT_1=$GRID_PORT-1))
((GRID_PORT_2=$GRID_PORT-2))

CURRENT_DIR=${PWD##*/}
PROJECT_NAME=`echo "$CURRENT_DIR" | awk '{print tolower($0)}'`
echo "PROJECT_NAME: ${PROJECT_NAME}"

export PROXY_KEY=$PROXY_KEY
export GRID_PORT=$GRID_PORT
export GRID_PORT_1=$GRID_PORT_1
export GRID_PORT_2=$GRID_PORT_2
export SELENIUM_HUB_REPO=$SELENIUM_HUB_REPO
export CHROME_REPO=$CHROME_REPO
export FIREFOX_REPO=$FIREFOX_REPO
export HOST_IP=$ip_address
export PROJECT_NAME=$PROJECT_NAME

echo "Using:"
echo "  PROJECT_NAME: $PROJECT_NAME"
echo "  HOST_IP: $ip_address"
echo "  PROXY_KEY: $PROXY_KEY"
echo "  SELENIUM_HUB_REPO: $SELENIUM_HUB_REPO"
echo "  CHROME_REPO: $CHROME_REPO"
echo "  FIREFOX_REPO: $FIREFOX_REPO"
echo "  GRID_PORT: $GRID_PORT"
echo "  GRID_PORT_1: $GRID_PORT_1"
echo "  GRID_PORT_2: $GRID_PORT_2"

DOCKER_COMPOSE_DOWN_CMD="docker-compose -f $DOCKER_COMPOSE_FILE_NAME -p ${PROJECT_NAME} down"
DOCKER_COMPOSE_UP_CMD="docker-compose -f $DOCKER_COMPOSE_FILE_NAME -p ${PROJECT_NAME} up --force-recreate -d"

if [[ ( $1 == "up" ) || ( $1 == "start" ) ]]; then
  echo "Start docker containers using command: '${DOCKER_COMPOSE_UP_CMD}'"
  ${DOCKER_COMPOSE_UP_CMD}
  ./wait_for_containers_to_be_up.sh
elif [[ ( $1 == "down" ) || ( $1 == "stop" ) ]]; then
  echo "Stop docker containers using command: '${DOCKER_COMPOSE_DOWN_CMD}'"
  ${DOCKER_COMPOSE_DOWN_CMD}
elif [[ ( $1 == "restart" ) ]]; then
  echo "Restarting docker containers using command: '${DOCKER_COMPOSE_DOWN_CMD}' and then '${DOCKER_COMPOSE_UP_CMD}'"
  ${DOCKER_COMPOSE_DOWN_CMD}
  sleep 2
  ${DOCKER_COMPOSE_UP_CMD}
  ./wait_for_containers_to_be_up.sh
else
  echo "Invalid command provided. Pass either 'up/start' or 'down/stop' as a parameter"
  exit 1
fi
