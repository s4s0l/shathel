#!/usr/bin/env bash
#export ANSIBLE_CONFIG=./ansible/ansible.cfg
export ANSIBLE_HOST_KEY_CHECKING=False
export TF_STATE=./state/terraform.tfstate
export ANSIBLE_NOCOWS=1
PARAM_USER=root
PARAM_KEY=$(pwd)/private/id_rsa
PARAM_EXTRA_VARS="
shathel_manager_hosts=shathel_manager
shathel_worker_hosts=shathel_worker
shathel_node_public_ip=ipv4_address
shathel_node_private_ip=ipv4_address_private
shathel_solution=shttmp
shathel_docker_machine_storage=$(pwd)/private
shathel_docker_machine_key=${PARAM_KEY}
shathel_docker_machine_user=${PARAM_USER}
"
ansible-playbook -u ${PARAM_USER} --private-key=${PARAM_KEY} --inventory-file=./bin/terraform-inventory ansible/playbook.yml --extra-vars "${PARAM_EXTRA_VARS}"