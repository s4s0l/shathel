#!/usr/bin/env bash
set -e
docker build -t dind4java:1.13.0 sandbox/did4java
docker run --privileged -w /build -v $(pwd):/build --rm dind4java:1.13.0 ./gradlew --gradle-user-home=.cache --project-cache-dir=.cache clean test clean
