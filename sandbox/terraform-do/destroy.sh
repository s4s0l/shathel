#!/usr/bin/env bash
./terraform.sh destroy -force
rm -fr ./private/machines
rm -fr ./private/certs
rm -f ./private/id_rsa
rm -f ./private/id_rsa.pub