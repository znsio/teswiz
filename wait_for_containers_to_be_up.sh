#!/bin/bash

set -o nounset

trap 'fail "The execution was aborted because a command exited with an error status code."' ERR

function fail {
  echo $1 >&2
  exit 1
}

function retry {
 local n=1
 local max=25
 local delay=3
 while true; do
 "$@" && break || {
    if [[ $n -lt $max ]]; then
        ((n++))
        echo "Command failed. Attempt $n/$max:"
        sleep $delay;
        else
        fail "The command has failed after $n attempts."
    fi
 }
 done
}

function wait_for_appium_server {
    echo ""
    echo "Waiting for appium server"
    retry curl --noproxy '*' -f http://0.0.0.0:${APPIUM_PORT}/wd/hub/sessions -v
}

function wait_for_selenium_hub {
    echo ""
    echo "Waiting for selenium hub"
    retry curl --noproxy '*' -f http://127.0.0.1:${GRID_PORT} -v
}

wait_for_appium_server
wait_for_selenium_hub
 
echo ""
echo "Appium Server and Selenium hub are now up and ready"