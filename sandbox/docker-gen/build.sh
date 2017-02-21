#!/usr/bin/env bash
set -e
MYPATH=$(pwd)
cd ~/Projects/gopath/src/github.com/s4s0l/docker-gen
export CGO_ENABLED=0
make dist
cd ${MYPATH}
cp /home/sasol/Projects/gopath/src/github.com/s4s0l/docker-gen/dist/alpine-linux/amd64/docker-gen  ./docker-gen

docker build -t 111.111.111.99:4000/sasol/docker-gen:latest ./docker-gen
docker push 111.111.111.99:4000/sasol/docker-gen:latest