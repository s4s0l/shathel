#!/usr/bin/env bash
#derived from https://github.com/raykrueger/docker-swarm-mode-scripts
set -e
if [[ ( -z ${1} ) || ( -z ${2} ) || ( -z ${3} ) ]]; then
    echo "Usage: swarm-cluster-create.sh CLUSTER_NAME NUMBER_OF_MANAGERS NUMBER_OF_WORKERS"
    exit 1
fi
CLUSTER_NAME=$1
NUMBER_OF_MANAGERS=$2
NUMBER_OF_WORKERS=$3
HOST_ONLY_ADDR=20.20.20.254
HOST_ONLY_CIDR=${HOST_ONLY_ADDR}/24


function distribute_Keys(){
    echo "---Distributing keys to $1"
    docker-machine scp /tmp/${CLUSTER_NAME}-registry/mirrorcerts/ca.crt $1:/tmp/mirror-ca.crt
    docker-machine ssh $1 sudo mkdir -p /etc/docker/certs.d/${MANAGER_IP}:4001/
    docker-machine ssh $1 sudo cp /tmp/mirror-ca.crt /etc/docker/certs.d/${MANAGER_IP}:4001/ca.crt

    docker-machine scp /tmp/${CLUSTER_NAME}-registry/certs/ca.crt $1:/tmp/repo-ca.crt
    docker-machine ssh $1 sudo mkdir -p /etc/docker/certs.d/${MANAGER_IP}:4000/
    docker-machine ssh $1 sudo cp /tmp/repo-ca.crt /etc/docker/certs.d/${MANAGER_IP}:4000/ca.crt
}





echo "---Create $CLUSTER_NAME-manager-1"
docker-machine create -d virtualbox --engine-registry-mirror https://${CLUSTER_NAME}-manager-1:4001 --virtualbox-hostonly-cidr=${HOST_ONLY_CIDR} ${CLUSTER_NAME}-manager-1
MANAGER_IP=$(docker-machine ip ${CLUSTER_NAME}-manager-1)


echo "---Generate Certificates for mirror and repository"
mkdir -p /tmp/${CLUSTER_NAME}-registry/mirrorcerts
mkdir -p /tmp/${CLUSTER_NAME}-registry/certs
openssl req -newkey rsa:4096 -nodes -sha256 -x509 -days 365 -subj "/CN=${MANAGER_IP}" \
         -extensions v3_xxx -config \
     	    <(cat /usr/lib/ssl/openssl.cnf \
         	<(printf "[ v3_xxx ]\nbasicConstraints = critical, CA:true\nkeyUsage=critical, digitalSignature,keyEncipherment,keyAgreement,dataEncipherment,keyCertSign\nextendedKeyUsage=critical,serverAuth,clientAuth,codeSigning\nsubjectAltName=IP:${MANAGER_IP},DNS:${CLUSTER_NAME}-manager-1,DNS:dregistry.${CLUSTER_NAME}")) \
        -keyout /tmp/${CLUSTER_NAME}-registry/mirrorcerts/domain.key \
        -out /tmp/${CLUSTER_NAME}-registry/mirrorcerts/ca.crt

#        -extensions SAN \
#        -config <(cat /usr/lib/ssl/openssl.cnf \
#            <(printf "[SAN]\nsubjectAltName=IP:${MANAGER_IP},DNS:${CLUSTER_NAME}-manager-1,DNS:dregistry.${CLUSTER_NAME}")) \
openssl req -newkey rsa:4096 -nodes -sha256 -x509 -days 365 -subj "/CN=${MANAGER_IP}" \
         -extensions v3_xxx -config \
     	    <(cat /usr/lib/ssl/openssl.cnf \
         	<(printf "[ v3_xxx ]\nbasicConstraints = critical, CA:true\nkeyUsage=critical, digitalSignature,keyEncipherment,keyAgreement,dataEncipherment,keyCertSign\nextendedKeyUsage=critical,serverAuth,clientAuth,codeSigning\nsubjectAltName=IP:${MANAGER_IP},DNS:${CLUSTER_NAME}-manager-1,DNS:dregistry.${CLUSTER_NAME}")) \
        -keyout /tmp/${CLUSTER_NAME}-registry/certs/domain.key \
        -out /tmp/${CLUSTER_NAME}-registry/certs/ca.crt

sudo mkdir -p /etc/docker/certs.d/${MANAGER_IP}:4000/
sudo mkdir -p /etc/docker/certs.d/${MANAGER_IP}:4001/
sudo cp /tmp/${CLUSTER_NAME}-registry/certs/ca.crt /etc/docker/certs.d/${MANAGER_IP}:4000/ca.crt
sudo cp /tmp/${CLUSTER_NAME}-registry/mirrorcerts/ca.crt /etc/docker/certs.d/${MANAGER_IP}:4001/ca.crt



distribute_Keys ${CLUSTER_NAME}-manager-1

docker-machine scp /tmp/${CLUSTER_NAME}-registry/mirrorcerts/ca.crt ${CLUSTER_NAME}-manager-1:/tmp/mirror-ca.crt
docker-machine ssh ${CLUSTER_NAME}-manager-1 sudo mkdir -p /etc/docker/certs.d/${CLUSTER_NAME}-manager-1:4001/
docker-machine ssh ${CLUSTER_NAME}-manager-1 sudo cp /tmp/mirror-ca.crt /etc/docker/certs.d/${CLUSTER_NAME}-manager-1:4001/ca.crt

docker-machine ssh ${CLUSTER_NAME}-manager-1 sudo mkdir -p /registry/certs
docker-machine ssh ${CLUSTER_NAME}-manager-1 sudo mkdir -p /registry/mirrorcerts
docker-machine ssh ${CLUSTER_NAME}-manager-1 sudo chown -R docker /registry

docker-machine scp /tmp/${CLUSTER_NAME}-registry/mirrorcerts/ca.crt ${CLUSTER_NAME}-manager-1:/registry/mirrorcerts/ca.crt
docker-machine scp /tmp/${CLUSTER_NAME}-registry/mirrorcerts/domain.key ${CLUSTER_NAME}-manager-1:/registry/mirrorcerts/domain.key

docker-machine scp /tmp/${CLUSTER_NAME}-registry/certs/ca.crt ${CLUSTER_NAME}-manager-1:/registry/certs/ca.crt
docker-machine scp /tmp/${CLUSTER_NAME}-registry/certs/domain.key ${CLUSTER_NAME}-manager-1:/registry/certs/domain.key


echo "---Run mirror repository container"
docker-machine ssh ${CLUSTER_NAME}-manager-1 docker run -d --restart=always -p 4001:5000 --name mirror-registry \
 -v /registry/mirrordata:/var/lib/registry \
 -v /registry/mirrorcerts:/certs \
 -e REGISTRY_HTTP_TLS_CERTIFICATE=/certs/ca.crt \
 -e REGISTRY_HTTP_TLS_KEY=/certs/domain.key \
 -e REGISTRY_PROXY_REMOTEURL=https://registry-1.docker.io \
  registry:2.5

echo "---Run repository container"
docker-machine ssh ${CLUSTER_NAME}-manager-1 docker run -d --restart=always -p 4000:5000 --name registry \
 -v /registry/data:/var/lib/registry \
 -v /registry/certs:/certs \
 -e REGISTRY_HTTP_TLS_CERTIFICATE=/certs/ca.crt \
 -e REGISTRY_HTTP_TLS_KEY=/certs/domain.key \
  registry:2.5








echo "---Swarm Init"
docker-machine ssh ${CLUSTER_NAME}-manager-1 docker swarm init --listen-addr ${MANAGER_IP} --advertise-addr ${MANAGER_IP}




printf "\n---Get Tokens\n"
manager_token=$(docker-machine ssh ${CLUSTER_NAME}-manager-1 docker swarm join-token -q manager)
worker_token=$(docker-machine ssh ${CLUSTER_NAME}-manager-1 docker swarm join-token -q worker)
echo ${manager_token}
echo ${worker_token}



for n in $(seq 2  ${NUMBER_OF_MANAGERS}) ; do
	printf "\n---Create ${CLUSTER_NAME}-manager-${n}\n"
	docker-machine create -d virtualbox --engine-registry-mirror https://${MANAGER_IP}:4001 --virtualbox-hostonly-cidr=${HOST_ONLY_CIDR} ${CLUSTER_NAME}-manager-${n}
	distribute_Keys ${CLUSTER_NAME}-manager-${n}
	ip=$(docker-machine ip ${CLUSTER_NAME}-manager-${n})
	echo "--- Swarm Manager Join"
	docker-machine ssh ${CLUSTER_NAME}-manager-${n} docker swarm join --listen-addr ${ip} --advertise-addr ${ip} --token ${manager_token} ${MANAGER_IP}:2377
done

for n in $(seq 1  ${NUMBER_OF_WORKERS}) ; do
	printf "\n---Create ${CLUSTER_NAME}-worker-${n}\n"
	docker-machine create -d virtualbox --engine-registry-mirror https://${MANAGER_IP}:4001 --virtualbox-hostonly-cidr=${HOST_ONLY_CIDR} ${CLUSTER_NAME}-worker-${n}
	distribute_Keys ${CLUSTER_NAME}-worker-${n}
	ip=$(docker-machine ip ${CLUSTER_NAME}-worker-${n})
	echo "--- Swarm Worker Join"
	docker-machine ssh ${CLUSTER_NAME}-worker-${n} docker swarm join --listen-addr ${ip} --advertise-addr ${ip} --token ${worker_token} ${MANAGER_IP}:2377
done

printf "\n---Launching Portainer\n"
docker-machine ssh ${CLUSTER_NAME}-manager-1 docker service create \
    --name portainer \
    --publish 9000:9000 \
    --constraint 'node.role==manager' \
    --mount type=bind,src=/var/run/docker.sock,dst=/var/run/docker.sock \
    portainer/portainer \
    -H unix:///var/run/docker.sock

printf "\n\n------------------------------------\n"
echo "To connect to your cluster..."
echo 'eval $(docker-machine env ${CLUSTER_NAME}-manager-1)'
echo 'or: docker-machine use ${CLUSTER_NAME}-manager-1'
echo "To visualize your cluster..."
echo "Open a browser to http://${MANAGER_IP}:9000/"