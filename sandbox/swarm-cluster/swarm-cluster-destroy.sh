#!/usr/bin/env bash
set -e
if [[ ( -z ${1} ) ]]; then
    echo "Usage: swarm-cluster-destroy.sh"
    exit 1
fi
CLUSTER_NAME=$1





docker-machine rm -f $(docker-machine ls -q | grep ${CLUSTER_NAME}) || true
eval $(docker-machine env -u)
docker rm -f -v registry_mirror || true
