#!/usr/bin/env bash
#derived from https://github.com/raykrueger/docker-swarm-mode-scripts
set -e
if [[ ( -z ${1} ) || ( -z ${2} ) ]]; then
    echo "Usage: swarm-cluster-create.sh CLUSTER_NAME NUMBER_OF_NODES"
    exit 1
fi
CLUSTER_NAME=$1
NUMBER_OF_NODES=$2
HOST_ONLY_CIDR=192.168.100.1/24


for n in $(seq 1  ${NUMBER_OF_NODES}) ; do
	printf "\n---Create r-${CLUSTER_NAME}-node-${n}\n"
	docker-machine create -d virtualbox --virtualbox-hostonly-cidr=${HOST_ONLY_CIDR} r-${CLUSTER_NAME}-node-${n}
	ip=$(docker-machine ip r-${CLUSTER_NAME}-node-${n})
	docker-machine ssh r-${CLUSTER_NAME}-node-${n} sudo mkdir /mnt/sda1/var/lib/rancher
    docker-machine ssh r-${CLUSTER_NAME}-node-${n} sudo mkdir /var/lib/rancher
    docker-machine ssh r-${CLUSTER_NAME}-node-${n} sudo mount -r /mnt/sda1/var/lib/rancher /var/lib/rancher
done
eval $(docker-machine env r-${CLUSTER_NAME}-node-1)
docker run -d --restart=always --name=rancher-server -p 8080:8080 rancher/server:v1.4.0-rc1

printf "\n\n------------------------------------\n"
echo "To connect to your cluster..."
echo 'eval $(docker-machine env r-${CLUSTER_NAME}-node-1)'
echo 'docker-machine use r-${CLUSTER_NAME}-node-1'
echo "To visualize your cluster..."
echo "Open a browser to http://$(docker-machine ip r-${CLUSTER_NAME}-node-1):8080/"