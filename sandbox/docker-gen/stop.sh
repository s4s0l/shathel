#!/usr/bin/env bash
set -e
. ./env.sh

MACHINE=$(echo $MACHINES | sed "s/,.*//g")
eval $(docker-machine ${MACHINE_OPTS} env ${MACHINE})

docker stack rm helloworld
docker stack rm socating
