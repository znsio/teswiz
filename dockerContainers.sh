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
echo "Using GRID_PORT: $GRID_PORT"

if [[ -z "${HTTP_PROXY}" ]]; then
  echo "HTTP_PROXY is not set."
  PROXY_KEY=NOT_SET
else
  PROXY_KEY=$HTTP_PROXY
fi
echo "Using PROXY_KEY: $PROXY_KEY"

((GRID_PORT_1=$GRID_PORT-1))
((GRID_PORT_2=$GRID_PORT-2))
export PROXY_KEY=$PROXY_KEY
export GRID_PORT=$GRID_PORT
export GRID_PORT_1=$GRID_PORT_1
export GRID_PORT_2=$GRID_PORT_2
export SELENIUM_HUB_REPO=$SELENIUM_HUB_REPO
export CHROME_REPO=$CHROME_REPO
export FIREFOX_REPO=$FIREFOX_REPO

echo "Using SELENIUM_HUB_REPO: $SELENIUM_HUB_REPO"
echo "Using CHROME_REPO: $CHROME_REPO"
echo "Using FIREFOX_REPO: $FIREFOX_REPO"
echo "GRID_PORT: $GRID_PORT"
echo "GRID_PORT_1: $GRID_PORT_1"
echo "GRID_PORT_2: $GRID_PORT_2"

if [[ ( $1 == "up" ) || ( $1 == "start" ) ]]; then
    echo "Start docker containers"
    docker-compose -f docker-compose-v3.yml up -d
    ./wait_for_containers_to_be_up.sh
elif [[ ( $1 == "down" ) || ( $1 == "stop" ) ]]; then
    echo "Stop docker containers"
    docker-compose -f docker-compose-v3.yml down
else
    echo "Invalid command provided. Pass either 'up/start' or 'down/stop' as a parameter"
    exit 1
fi