#!/usr/bin/env bash

FILE_PATH=$(readlink -e $1)
echo $(TMP=$(dirname ${FILE_PATH}) && echo ${TMP/\/home\//\/hosthome/})/$(basename ${FILE_PATH})