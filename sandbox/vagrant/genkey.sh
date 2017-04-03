#!/usr/bin/env bash
if [ ! -f ./private/id_rsa ]; then
    ssh-keygen -t rsa -C "mwielgus@outlook.com" -N "" -f ./private/id_rsa
fi

