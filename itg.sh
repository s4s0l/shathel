#!/bin/bash
set -e
gradle :shathel-deployer:install

export SHATHEL_FILE_BASE_DIR=$(pwd)/../shathel-stacks
export SHATHEL_DEPLOYER_DIR=$(pwd)/build/playground
export SHATHEL_ENV=local
export SHATHEL_ENV_ITG_FORCEFUL=true
export SHATHEL_ENV_LOCAL_FORCEFUL=true
export SHATHEL_ENV_ITG_NET=111.111.111
export SHATHEL_ENV_ITG_SAFEPASSWORD=HelloWorld
export JAVA_OPTS=-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005
./build/localrepo/org/s4s0l/shathel/shathel-deployer/DEVELOPER-SNAPSHOT/shathel-deployer-DEVELOPER-SNAPSHOT.jar

