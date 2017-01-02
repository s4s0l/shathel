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
HOST_ONLY_CIDR=20.20.20.254/24


echo "---Create $CLUSTER_NAME-manager-1"
docker-machine create -d virtualbox --virtualbox-hostonly-cidr=${HOST_ONLY_CIDR} ${CLUSTER_NAME}-manager-1
manager_ip=$(docker-machine ip ${CLUSTER_NAME}-manager-1)

echo "---Swarm Init"
docker-machine ssh ${CLUSTER_NAME}-manager-1 docker swarm init --listen-addr ${manager_ip} --advertise-addr ${manager_ip}

printf "\n---Get Tokens\n"
manager_token=$(docker-machine ssh ${CLUSTER_NAME}-manager-1 docker swarm join-token -q manager)
worker_token=$(docker-machine ssh ${CLUSTER_NAME}-manager-1 docker swarm join-token -q worker)
echo ${manager_token}
echo ${worker_token}


for n in $(seq 2  ${NUMBER_OF_MANAGERS}) ; do
	printf "\n---Create ${CLUSTER_NAME}-manager-${n}\n"
	docker-machine create -d --virtualbox-hostonly-cidr=${HOST_ONLY_CIDR} virtualbox ${CLUSTER_NAME}-manager-${n}
	ip=$(docker-machine ip ${CLUSTER_NAME}-manager-${n})
	echo "--- Swarm Manager Join"
	docker-machine ssh ${CLUSTER_NAME}-manager-${n} docker swarm join --listen-addr ${ip} --advertise-addr ${ip} --token ${manager_token} ${manager_ip}:2377
done

for n in $(seq 1  ${NUMBER_OF_WORKERS}) ; do
	printf "\n---Create ${CLUSTER_NAME}-worker-${n}\n"
	docker-machine create -d virtualbox --virtualbox-hostonly-cidr=${HOST_ONLY_CIDR} ${CLUSTER_NAME}-worker-${n}
	ip=$(docker-machine ip ${CLUSTER_NAME}-worker-${n})
	echo "--- Swarm Worker Join"
	docker-machine ssh ${CLUSTER_NAME}-worker-${n} docker swarm join --listen-addr ${ip} --advertise-addr ${ip} --token ${worker_token} ${manager_ip}:2377
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
echo "Open a browser to http://${manager_ip}:9000/"