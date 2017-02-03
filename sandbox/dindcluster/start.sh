#!/usr/bin/env bash
set -e
docker rm -f -v x-dev-manager-1 | true
docker rm -f -v x-dev-manager-2| true
docker rm -f -v x-dev-worker-1| true

docker run --privileged --label org.shathel.env.dind=true --name x-dev-manager-1 -d docker:1.13.0-dind

MANIP=$(docker inspect -f {{.NetworkSettings.Networks.bridge.IPAddress}} x-dev-manager-1)
docker --host $MANIP swarm init

docker run --privileged --label org.shathel.env.dind=true  --name x-dev-manager-2 -d docker:1.13.0-dind
MANTOK=$(docker --host $MANIP swarm join-token -q manager)
docker --host $(docker inspect -f {{.NetworkSettings.Networks.bridge.IPAddress}} x-dev-manager-2) \
    swarm join \
    --token $MANTOK \
    ${MANIP}:2377


docker run --privileged --name x-dev-worker-1 -d docker:1.13.0-dind
MANTOK=$(docker --host $MANIP swarm join-token -q worker)
docker --host $(docker inspect -f {{.NetworkSettings.Networks.bridge.IPAddress}} x-dev-worker-1) \
    swarm join \
    --token $MANTOK \
    ${MANIP}:2377




SUPER WAŻNE NA HOŚCIE MUSI BYĆ sudo modprobe xt_ipvs
