#!/usr/bin/env bash
if [ ! -f ./private/id_rsa ]; then
    ssh-keygen -t rsa -C "mwielgus@outlook.com" -N "" -f ./private/id_rsa
fi
#
#openssl genrsa -aes256 -out ./private/shathel-ca-key.pem 4096
#
#openssl req -new -x509 -days 365 -key ./private/shathel-ca-key.pem -sha256 -out ./private/shathel-ca.pem
#
#
#openssl genrsa -out ./private/some-server-key.pem 4096
#openssl req -subj "/CN=my.dns.name" -sha256 -new -key ./private/some-server-key.pem -out ./private/some-server.csr
#
#echo subjectAltName = DNS:my.dns.name,IP:10.10.10.20,IP:127.0.0.1 > ./private/extfile.cnf
#
#openssl x509 -req -days 365 -sha256 -in ./private/some-server.csr -CA ./private/shathel-ca.pem -CAkey ./private/shathel-ca-key.pem \
#  -CAcreateserial -out ./private/some-server-cert.pem -extfile ./private/extfile.cnf
#
