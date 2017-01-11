#!/usr/bin/env bash
set -e
MACHINE_ENV=$1
IMAGE_TO_RUN=$2

REPO_NAME=$(docker-machine ip $(docker-machine ls -q --filter name=${MACHINE_ENV}-manager-1)):4000



RUNCOMMANDS=${@:2}
RUNCOMMANDS=${RUNCOMMANDS//_REPO_/${REPO_NAME}}

echo $RUNCOMMANDS
#docker $RUNCOMMANDS


