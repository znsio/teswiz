#!/bin/bash

set -e

cmd="$@"
attempt=0
while ! curl -sSL "http://localhost:${GRID_PORT}/wd/hub/status" 2>&1 \
        | jq -r '.value.ready' 2>&1 | grep "true" >/dev/null; do
    echo "Waiting for the Grid to be available on: http://localhost:${GRID_PORT}/wd/hub/status"
    sleep 3
    attempt+=1
    if [ $attempt -gt 10 ]; then
        echo "Grid not started after $attempt attempts. Abort"
        exit 1;
    fi
done

>&2 echo "Selenium Grid is up and ready to run tests"
exec $cmd