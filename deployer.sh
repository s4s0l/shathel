#!/bin/bash
gradle :shathel-deployer:install

export SHATHEL_MVN_LOCALREPO=$(pwd)/build/localrepo
export SHATHEL_DEPLOYER_DIR=$(pwd)/build/playground
export SHATHEL_ENV=dev
export SHATHEL_ENV_DEV_FORCEFUL=true
export SHATHEL_ENV_DEV_NET=99.99.99


./build/localrepo/org/s4s0l/shathel/shathel-deployer/DEVELOPER-SNAPSHOT/shathel-deployer-DEVELOPER-SNAPSHOT.jar


#cd /home/sasol/Projects/shathel-swarm/
#~ gradle :shathel-stack-core:install
#~ gradle :shathel-stack-portainer:install
