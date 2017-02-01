#!/bin/sh
set -x
dockerd --host=unix:///var/run/docker.sock --storage-driver=vfs &
pid="$!"
echo "Got pid=$pid"
# wait forever
while true; do
  docker version && break
  sleep 1
done
$@