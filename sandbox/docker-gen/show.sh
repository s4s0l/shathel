#!/usr/bin/env bash
set -e
. ./env.sh

MACHINE=$(echo $MACHINES | sed "s/,.*//g")
docker-machine ${MACHINE_OPTS} ssh ${MACHINE} sudo cat /mnt/sda1/nginx-conf.d/default.conf