#!/bin/bash

set -e

arch="$(uname -m)"  # -i is only linux, -m is linux and apple
echo "Running on arch: $arch"
DOCKER_REGISTRY="selenium"
CHROME_REPO="node-chrome"
FIREFOX_REPO="node-firefox"
SELENIUM_HUB_REPO="hub"

if [[ "$arch" = x86_64* ]]; then
    if [[ "$(uname -a)" = *ARM64* ]]; then
        echo 'a64'
        DOCKER_REGISTRY="seleniarm"
        CHROME_REPO="node-chromium"
    else
        echo 'x64'
    fi
elif [[ "$arch" = i*86 ]]; then
    echo 'x32'
elif [[ "$arch" = arm* ]]; then
    echo 'a32'
    DOCKER_REGISTRY="seleniarm"
    CHROME_REPO="node-chromium"
elif test "$arch" = aarch64; then
    echo 'a64'
    DOCKER_REGISTRY="seleniarm"
    CHROME_REPO="node-chromium"
else
    exit 1
fi

echo "Using the containers from DOCKER_REGISTRY: $DOCKER_REGISTRY for architecture: $arch"

export DOCKER_REGISTRY=$DOCKER_REGISTRY

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
export PROXY_KEY=$PROXY_KEY
export GRID_PORT=$GRID_PORT
export GRID_PORT_1=$GRID_PORT_1
export GRID_PORT_2=$GRID_PORT_2
export SELENIUM_HUB_REPO=$SELENIUM_HUB_REPO
export CHROME_REPO=$CHROME_REPO
export FIREFOX_REPO=$FIREFOX_REPO

echo "Using:"
echo "  PROXY_KEY: $PROXY_KEY"
echo "  SELENIUM_HUB_REPO: $SELENIUM_HUB_REPO"
echo "  CHROME_REPO: $CHROME_REPO"
echo "  FIREFOX_REPO: $FIREFOX_REPO"
echo "  GRID_PORT: $GRID_PORT"
echo "  GRID_PORT_1: $GRID_PORT_1"
echo "  GRID_PORT_2: $GRID_PORT_2"

CURRENT_DIR=${PWD##*/}
echo "CURRENT_DIR: ${CURRENT_DIR}"

DOCKER_COMPOSE_UP_CMD="docker-compose -f $DOCKER_COMPOSE_FILE_NAME -p ${CURRENT_DIR} down"
DOCKER_COMPOSE_DOWN_CMD="docker-compose -f $DOCKER_COMPOSE_FILE_NAME -p ${CURRENT_DIR} up --force-recreate -d"

if [[ ( $1 == "up" ) || ( $1 == "start" ) ]]; then
    echo "Start docker containers using command: '${DOCKER_COMPOSE_DOWN_CMD}'"
    ${DOCKER_COMPOSE_DOWN_CMD}
    ./wait_for_containers_to_be_up.sh
elif [[ ( $1 == "down" ) || ( $1 == "stop" ) ]]; then
    echo "Stop docker containers using command: '${DOCKER_COMPOSE_UP_CMD}'"
    ${DOCKER_COMPOSE_UP_CMD}
elif [[ ( $1 == "restart" ) ]]; then
    echo "Restarting docker containers using command: '${DOCKER_COMPOSE_UP_CMD}' and then '${DOCKER_COMPOSE_DOWN_CMD}'"
    ${DOCKER_COMPOSE_UP_CMD}
    sleep 2
    ${DOCKER_COMPOSE_DOWN_CMD}
else
    echo "Invalid command provided. Pass either 'up/start' or 'down/stop' as a parameter"
    exit 1
fi