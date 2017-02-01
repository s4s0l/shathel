#!/usr/bin/env bash
set -e
docker build -t dind4java:1.13.0 sandbox/did4java
docker run --privileged --name some-docker -w /build -v $(pwd):/build --rm -ti dind4java:1.13.0 ./gradlew test
docker run --privileged --name some-docker -w /build -v $(pwd):/build --rm -ti dind4java:1.13.0 ./gradlew clean