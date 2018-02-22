#!/bin/bash
set -e
./gradlew :shathel-deployer:install

export SHATHEL_SOLUTION_DOCKER_VERSION=17.12.0
export SHATHEL_SOLUTION_FILE_BASE_DIR=$(pwd)/../shathel-stacks
export SHATHEL_SOLUTION_FILE_ENV_BASE_DIR=$(pwd)/../shathel-envs
export SHATHEL_DIR=$(pwd)/build/playground
export SHATHEL_ENV=local



export SHATHEL_ENV_LOCAL_FORCEFUL=true
#export SHATHEL_ENV_LOCAL_SAFEPASSWORD=HelloWorld



export SHATHEL_ENV_LOCAL_TEST_FORCEFUL=true
export SHATHEL_ENV_LOCAL_TEST_SAFEPASSWORD=HelloWorld

export SHATHEL_ENV_VBOX_TYPE=remote
export SHATHEL_ENV_VBOX_GAV=./virtualbox
export SHATHEL_ENV_VBOX_FORCEFUL=true
export SHATHEL_ENV_VBOX_DOMAIN=shathel-vbox
export SHATHEL_ENV_VBOX_SAFEPASSWORD=HelloWorld
export SHATHEL_ENV_VBOX_MANAGERS=1
export SHATHEL_ENV_VBOX_WORKERS=0



export SHATHEL_ENV_AWS_TYPE=remote
export SHATHEL_ENV_AWS_GAV=./aws
export SHATHEL_ENV_AWS_FORCEFUL=true
export SHATHEL_ENV_AWS_SAFEPASSWORD=HelloWorld
export SHATHEL_ENV_AWS_MANAGERS=2
export SHATHEL_ENV_AWS_WORKERS=1

export SHATHEL_ENV_DO_TYPE=remote
export SHATHEL_ENV_DO_GAV=./digital-ocean
export SHATHEL_ENV_DO_FORCEFUL=true
export SHATHEL_ENV_DO_SAFEPASSWORD=HelloWorld
export SHATHEL_ENV_DO_MANAGERS=2
export SHATHEL_ENV_DO_WORKERS=1

PRIVATE_VARS=$(cat private.properties | sed -e 's!\([^=]*\)!\U\1!;:a;s!\([^\.]\+\)\.\(.*=.*\)\+!\1_\2!;ta;')
eval "${PRIVATE_VARS}"
export $(echo "${PRIVATE_VARS}" | cut -d= -f1)

export JAVA_OPTS="-Xmx1024m -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005"
chmod +x ./build/localrepo/org/s4s0l/shathel/shathel-deployer/DEVELOPER-SNAPSHOT/shathel-deployer-DEVELOPER-SNAPSHOT.jar
./build/localrepo/org/s4s0l/shathel/shathel-deployer/DEVELOPER-SNAPSHOT/shathel-deployer-DEVELOPER-SNAPSHOT.jar

