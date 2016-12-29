#!/usr/bin/env bash
set -e
if [[ ( -z ${1} ) ]]; then
    echo "Usage: swarm-cluster-destroy.sh"
    exit 1
fi
CLUSTER_NAME=$1

docker-machine ls -q | grep ${CLUSTER_NAME}- | while read x; do
    eval $(docker-machine env ${x})
    docker "${@:2}"
done