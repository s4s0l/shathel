#!/usr/bin/env bash
#export ANSIBLE_CONFIG=./ansible/ansible.cfg
export ANSIBLE_HOST_KEY_CHECKING=False
export TF_STATE=./state/terraform.tfstate
export ANSIBLE_NOCOWS=1
PARAM_USER=ubuntu
PARAM_KEY=$(pwd)/private/id_rsa
PARAM_EXTRA_VARS="
shathel_manager_hosts=shathel_manager_hosts
shathel_worker_hosts=shathel_worker_hosts
shathel_node_public_ip=public_ip
shathel_node_private_ip=private_ip
shathel_solution=shttmp
shathel_docker_machine_storage=$(pwd)/private
shathel_docker_machine_key=${PARAM_KEY}
shathel_docker_machine_user=${PARAM_USER}
"
PLAYBOOK=./ansible/playbook.yml
INVENTORY=./state/ansible-inventory.ini
ansible-playbook -u ${PARAM_USER} --private-key=${PARAM_KEY} --inventory-file=${INVENTORY} ${PLAYBOOK} --extra-vars "${PARAM_EXTRA_VARS}"