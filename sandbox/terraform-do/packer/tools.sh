#!/usr/bin/env bash
sudo apt-get update
sudo apt-get install -y apt-transport-https ca-certificates nfs-common
curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo apt-key add -
sudo add-apt-repository "deb [arch=amd64] https://download.docker.com/linux/ubuntu $(lsb_release -cs) stable"
sudo apt-get update
sudo apt-get install -y docker-ce
sudo apt-get install -y htop
sudo apt-get install -y tree
sudo apt-get install -y jq
sudo apt-get install -y fail2ban
sudo apt-get install -y vim
sudo apt-get install -y mosh
sudo apt-get install -y ufw
sudo apt-get install -y python

