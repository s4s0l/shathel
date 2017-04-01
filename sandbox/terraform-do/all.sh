#!/usr/bin/env bash
set -e
./genkey.sh
./terraform.sh apply
./ansible.sh