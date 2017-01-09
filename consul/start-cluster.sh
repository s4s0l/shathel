#!/usr/bin/env bash
eval $(docker-machine env consul-manager-1)

docker network create consul-net -d overlay

docker service create --name consul \
--network consul-net \
--mode global \
-p 8500:8500 \
-e 'CONSUL_BIND_INTERFACE=eth0' \
-e 'CONSUL_LOCAL_CONFIG={"skip_leave_on_interrupt": false}' \
consul agent -server -ui -client=0.0.0.0 -bootstrap-expect=2 \
-retry-join=consul

docker service create --name consul-client \
--network consul-net \
-e 'CONSUL_BIND_INTERFACE=eth0' \
-e 'CONSUL_LOCAL_CONFIG={"leave_on_terminate": true}' \
consul agent -retry-join=consul