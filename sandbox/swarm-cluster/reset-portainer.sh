#!/usr/bin/env bash
set -e
CLUSTER_NAME=consul

printf "\n---Launching Portainer\n"
docker-machine ssh ${CLUSTER_NAME}-manager-1 docker service rm \
    portainer || true
docker-machine ssh ${CLUSTER_NAME}-manager-1 docker service create \
    --name portainer \
    --publish 9000:9000 \
    --constraint 'node.role==manager' \
    --mount type=bind,src=/var/run/docker.sock,dst=/var/run/docker.sock \
    portainer/portainer \
    -H unix:///var/run/docker.sock

./portainer-setup.groovy