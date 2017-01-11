#!/usr/bin/env bash
set -e
MACHINE_ENV=$1
FILE_TO_BUILD=$(readlink -e $2)

REPO_NAME=$(docker-machine ip $(docker-machine ls -q --filter name=${MACHINE_ENV}-manager-1)):4000
TAG=${REPO_NAME}/$(basename ${FILE_TO_BUILD}):latest
docker build -t ${TAG} ${FILE_TO_BUILD}
docker push ${TAG}


