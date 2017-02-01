#!/bin/bash
set -e
DAEMONPID=0
trap 'echo SIGTERM; kill ${!}; kill $DAEMONPID; exit 143' SIGTERM
trap 'echo SIGKILL; kill ${!}; kill $DAEMONPID; exit 137' SIGKILL
trap 'echo SIGINT;  kill ${!}; kill $DAEMONPID; exit 130' INT
dockerd --host=unix:///var/run/docker.sock --storage-driver=vfs &
DAEMONPID="$!"
while true; do
  docker version && break
  sleep 1
done

if [ -z ${RUNASUID+x} ]; then
    if [ -z ${INTERACTIVE+x} ]; then
        su -c "$*" & wait ${!}
    else
        su -c "$*"
    fi
else
    adduser -u 1000 -D user docker
    if [ -z ${INTERACTIVE+x} ]; then
        su user -c "$*" & wait ${!}
    else
        su user -c "$*"
    fi
fi
