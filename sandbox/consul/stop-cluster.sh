#!/usr/bin/env bash
eval $(docker-machine env consul-manager-1)
docker service rm consul
docker service rm consul-client
docker network rm consul-net
