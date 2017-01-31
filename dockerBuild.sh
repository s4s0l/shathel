#!/usr/bin/env bash
docker build -t dind4java:1.13.0 sandbox/did4java
docker run --privileged --name some-docker  -d --rm -ti docker:1.13.0-dind
docker run --rm --link some-docker:docker -w /build -v $(pwd):/build dind4java:1.13.0 ls -al