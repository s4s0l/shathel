#!/usr/bin/env bash
set -e
. ./env.sh

SOCAT_NAME=dockerexposer

for i in $(echo $MACHINES | sed "s/,/ /g")
do
    docker-machine ${MACHINE_OPTS} ssh ${i} sudo mkdir -p /mnt/sda1/nginx-templates
    docker-machine ${MACHINE_OPTS} ssh ${i} sudo mkdir -p /mnt/sda1/nginx-conf.d
    docker-machine ${MACHINE_OPTS} ssh ${i} sudo chmod a+rw /mnt/sda1/nginx-templates
    docker-machine ${MACHINE_OPTS} scp  ./nginx-swarm.tmpl ${i}:/mnt/sda1/nginx-templates/nginx-swarm.tmpl
done


mkdir -p ./tmp/
cp --remove-destination ./openssl.cnf ./tmp/openssl.cnf
echo "[ v3_xxx ]
basicConstraints = critical, CA:true
keyUsage=critical, digitalSignature,keyEncipherment,keyAgreement,dataEncipherment,keyCertSign
extendedKeyUsage=critical,serverAuth,clientAuth,codeSigning
subjectAltName=DNS:$SOCAT_NAME,DNS:tasks.$SOCAT_NAME,IP:111.111.111.99" >> ./tmp/openssl.cnf


openssl req -newkey rsa:1024 -nodes -sha256 -x509 -days 365 -subj /CN=${SOCAT_NAME} \
            -extensions v3_xxx -config ./tmp/openssl.cnf \
            -keyout ./tmp/server.key \
            -out ./tmp/server.crt

openssl req -newkey rsa:1024 -nodes -sha256 -x509 -days 365 -subj /CN=${SOCAT_NAME} \
            -extensions v3_xxx -config ./tmp/openssl.cnf \
            -keyout ./tmp/client.key \
            -out ./tmp/client.crt


cat ./tmp/server.key ./tmp/server.crt > ./tmp/server.pem
cat ./tmp/client.key ./tmp/client.crt > ./tmp/client.pem
openssl dhparam -out ./tmp/dhparams.pem 1024
cat ./tmp/dhparams.pem >> ./tmp/server.pem
