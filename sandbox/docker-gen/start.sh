#!/usr/bin/env bash
set -e
. ./env.sh

MACHINE=$(echo $MACHINES | sed "s/,.*//g")
eval $(docker-machine ${MACHINE_OPTS} env ${MACHINE})

docker stack deploy --compose-file docker-compose-socating.yml socating
docker stack deploy --compose-file docker-compose-service.yml helloworld