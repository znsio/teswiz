#!/bin/bash

set -e

((PORT_1=$GRID_PORT-1))
((PORT_2=$GRID_PORT-2))
export GRID_PORT_1=$PORT_1
export GRID_PORT_2=$PORT_2
echo $GRID_PORT
echo $GRID_PORT_1
echo $GRID_PORT_2

exec `docker-compose -f docker-compose-v3.yml up -d`

./wait_for_containers_to_be_up.sh