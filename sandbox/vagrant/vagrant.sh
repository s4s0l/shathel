export SHATHEL_VAGRANT_PERSISTENT_DIR=$(pwd)/state
export SHATHEL_VAGRANT_PACKAGE_VERSION=1.5

export SHATHEL_ANSIBLE_FILE=./state/ansible-inventory.ini
export SHATHEL_SSH_KEYS_LOCATION=./private
export SHATHEL_MANAGER_COUNT=1
export SHATHEL_WORKER_COUNT=0
export SHATHEL_SOLUTION_NAME=shtl-temp

export VAGRANT_DOTFILE_PATH=$(pwd)/state

vagrant $@