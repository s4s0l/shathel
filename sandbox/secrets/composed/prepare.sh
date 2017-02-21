#!/usr/bin/env bash

MACHINES=playground-itg-manager-1,playground-itg-manager-2,playground-itg-worker-1
MACHINE_OPTS="-s /home/sasol/Projects/shathel-swarm/build/playground/tmp/itg/settings"

SOCAT_NAME=dockerexposer

for i in $(echo $MACHINES | sed "s/,/ /g")
do
    docker-machine ${MACHINE_OPTS} scp  ./nginx.tmpl ${i}:/tmp/nginx.tmpl
    docker-machine ${MACHINE_OPTS} scp  ./ngnix.conf ${i}:/tmp/ngnix.conf
done


mkdir -p ./tmp/
cp --remove-destination ./openssl.cnf ./tmp/openssl.cnf
echo "[ v3_xxx ]
basicConstraints = critical, CA:true
keyUsage=critical, digitalSignature,keyEncipherment,keyAgreement,dataEncipherment,keyCertSign
extendedKeyUsage=critical,serverAuth,clientAuth,codeSigning
subjectAltName=DNS:$SOCAT_NAME,IP:111.111.111.99" >> ./tmp/openssl.cnf


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
