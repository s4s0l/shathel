#!/bin/bash
gradle :shathel-deployer:install

export SHATHEL_MVN_LOCALREPO=$(pwd)/build/localrepo
export SHATHEL_STORAGE_FILE=$(pwd)/build/playground
export SHATHEL_ENV=itg
export SHATHEL_STACK_FORCEFUL=true
export SHATHEL_ENV_ITG_NET=111.111.111


./build/localrepo/org/s4s0l/shathel/shathel-deployer/DEVELOPER-SNAPSHOT/shathel-deployer-DEVELOPER-SNAPSHOT.jar --continue\; cd $(pwd)


#cd /home/sasol/Projects/shathel-swarm/
#~ gradle :shathel-stack-core:install
#~ gradle :shathel-stack-portainer:install
