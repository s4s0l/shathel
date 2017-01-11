#!/usr/bin/env bash
#Installs docker-* tools on Mint 17.3
set -e
DISTRO_NAME=$(lsb_release -a 2>/dev/null | grep Codename: | cut -f2)
echo "DISTRO_NAME=${DISTRO_NAME}"
DOCKER_VERSION=1.13.0~rc5-0~ubuntu-${DISTRO_NAME/rosa/trusty}
echo "DOCKER_VERSION=${DOCKER_VERSION}"
COMPOSE_VERSION=1.10.0-rc1
echo "COMPOSE_VERSION=${COMPOSE_VERSION}"
MACHINE_VERSION=v0.9.0-rc2
echo "MACHINE_VERSION=${MACHINE_VERSION}"
apt-get -y install apt-transport-https ca-certificates
apt-key adv --keyserver hkp://p80.pool.sks-keyservers.net:80 --recv-keys 58118E89F3A912897C070ADBF76221572C52609D
echo "deb https://apt.dockerproject.org/repo ubuntu-${DISTRO_NAME/rosa/trusty} testing" > /etc/apt/sources.list.d/docker.list
apt-get update
apt-get purge lxc-docker
apt-cache policy docker-engine
apt-get -y install linux-image-extra-$(uname -r)
apt-get -y install docker-engine=$DOCKER_VERSION
service docker status | grep start/running  || service docker start
docker run hello-world
curl -L https://github.com/docker/compose/releases/download/$COMPOSE_VERSION/docker-compose-`uname -s`-`uname -m` > /usr/local/bin/docker-compose
chmod +x /usr/local/bin/docker-compose
curl -L https://raw.githubusercontent.com/docker/compose/$(docker-compose version --short)/contrib/completion/bash/docker-compose > /etc/bash_completion.d/docker-compose

curl -L https://github.com/docker/machine/releases/download/${MACHINE_VERSION}/docker-machine-`uname -s`-`uname -m` > /usr/local/bin/docker-machine
chmod +x /usr/local/bin/docker-machine
curl -L https://raw.githubusercontent.com/docker/machine/${MACHINE_VERSION}/contrib/completion/bash/docker-machine-wrapper.bash > /etc/bash_completion.d/docker-machine-wrapper
curl -L https://raw.githubusercontent.com/docker/machine/${MACHINE_VERSION}/contrib/completion/bash/docker-machine.bash > /etc/bash_completion.d/docker-machine


