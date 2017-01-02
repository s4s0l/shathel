#!/usr/bin/env bash
set -e
if [[ ( -z ${1} ) ]]; then
    echo "Usage: rancher-cluster-docker.sh CLUSTER_NAME docker_oprions...."
    exit 1
fi
CLUSTER_NAME=$1

docker-machine ls -q | grep r-${CLUSTER_NAME}-node | while read x; do
    eval $(docker-machine env ${x})
    docker "${@:2}"
done