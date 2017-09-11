#!/usr/bin/env bash
set -e
docker run --privileged \
    -e RUNASUID=$(id -u) \
    -w /build -v $(pwd):/build \
    --name shathel-builder \
    --rm sasol/dind4j:1.9 \
    ./gradlew --info --stacktrace --no-daemon --gradle-user-home=.cache --project-cache-dir=.cache clean test
