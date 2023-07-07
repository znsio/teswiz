#!/bin/bash

set -e

cmd="$@"
attempt=1
while ! curl -sSL "http://localhost:${GRID_PORT}/wd/hub/status" 2>&1 \
        | jq -r '.value.ready' 2>&1 | grep "true" >/dev/null; do
    echo "Attempt: $attempt - Waiting for the Grid to be available on:
    http://localhost:${GRID_PORT}/wd/hub/status"
    sleep 3
    ((attempt=attempt+1))
    if [ $attempt -gt 15 ]; then
        echo "Grid not started after $attempt attempts. Abort"
        exit 1;
    fi
done

>&2 echo "Selenium Grid is up and ready to run tests"
exec $cmd