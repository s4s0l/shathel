#!/usr/bin/env bash
set -e
PACKER_URL=https://releases.hashicorp.com/packer/0.12.3/packer_0.12.3_linux_
PACKER_DIR=./bin

if [[ ! -f $PACKER_DIR/packer ]]; then
    echo -e "${MSG}No packer, downloading...."
    archs=`uname -m`
    case "$archs" in
        i?86) PACKER_URL=${PACKER_URL}386.zip ;;
        x86_64) PACKER_URL=${PACKER_URL}amd64.zip ;;
        arm) PACKER_URL=${PACKER_URL}arm.zip ;;
    esac
    mkdir -p ${PACKER_DIR}/.tmp
    wget -O ${PACKER_DIR}/.tmp/packer.zip ${PACKER_URL}
    unzip ${PACKER_DIR}/.tmp/packer.zip -d ${PACKER_DIR}
    rm -f ${PACKER_DIR}/.tmp/packer.zip
fi

PACKER_CACHE_DIR=./private/packer/cache
PACKER_LOG=1
PACKER_LOG_PATH=./private/packer/log
TMPDIR=./private/packer/tmp
${PACKER_DIR}/packer build -var-file=./private/packer.vars.json ./packer/packer-ubuntu-docker.json