# Configuration concepts

# Settings
## Global settings
* shathel.env
* shathel.dir
* shathel.deployer.historyfile

## Solution level settings
* shathel.solution.name
* shathel.solution.forceful
* shathel.solution.docker_version 
* shathel.solution.file_default_version
* shathel.solution.file_default_group
* shathel.solution.file_base_dir
* shathel.solution.file_ignore_versions
* shathel.solution.git_default_version
* shathel.solution.git_default_group
* shathel.solution.file_env_default_version
* shathel.solution.file_env_default_group
* shathel.solution.file_env_base_dir
* shathel.solution.git_env_default_version
* shathel.solution.git_env_default_group
* shathel.solution.ivy_default_version
* shathel.solution_ivy_default_group
* shathel.solution.ivy_settings
* shathel.solution.ivy_repo_id
* shathel.solution.ivy_repos
* shathel.solution.ivy_user
* shathel.solution.ivy_pass
* shathel.solution.ivy_host
* shathel.solution.ivy_realm
* shathel.solution.docker_user
* shathel.solution.docker_pass
* shathel.solution.docker_host
* shathel.solution.dependenciesDir

## Environment level settings

We set them as shathel.env.{env}.net but they are accessible as shathel.env.net
in enrichers

* shathel.env.{env}.net

FIXME: below does not work 
* shathel.env.{env}.dataDir
* shathel.env.{env}.safeDir
* shathel.env.{env}.settingsDir
* shathel.env.{env}.enrichedDir
* shathel.env.{env}.tempDir
FIXME: above does not work

* shathel.env.{env}.pull
* shathel.env.{env}.domain

## Remote swarm environment
* shathel.env.{env}.managers
* shathel.env.{env}.workers
* shathel.env.{env}.useglobalvagrant

### VBOX
* shathel.env.{env}.private_net
* shathel.env.{env}.public_net

## Local Swarm environment
* shathel.env.{env}.ansible_become_password
* shathel.env.{env}.ansible_enabled // for local swarm



## Passwords settings
* shathel.env.{env}.safePassword
* shathel.env.{env}.{secretName}_secret_path
* shathel.env.{env}.{secretName}_secret_value


## How does it map to yml
```
version: 1
shathel-solution:
  sample1: value1
  envs:
    SAMPLE2: value2
  environments:
    local:
      sample3: value3
      envs:
        SAMPLE4: value4 
```
will set:
* shathel.solution.sample1
* SAMPLE2
* shathel.env.local.sample3
* SAMPLE4


## Gradle tests variables
* shathel.plugin.ip
* shathel.plugin.{stackName}.{serviceName}.{targetPort}
* shathel.plugin.local.override.mappings
* shathel.plugin.local.override.current
* shathel.plugin.current.gav
* shathel.plugin.current



# Environment scripts
* shathel.env.solution.name 
* SHATHEL_ENVPACKAGE_VERSION
* SHATHEL_ENVPACKAGE_SETTINGS_DIR
* SHATHEL_ENVPACKAGE_TMP_DIR
* SHATHEL_ENVPACKAGE_ANSIBLE_INVENTORY
* SHATHEL_ENVPACKAGE_USER
* SHATHEL_ENVPACKAGE_CERTS_DIR

## Remote environment RO
* SHATHEL_ENVPACKAGE_IMAGE_NAME
* SHATHEL_ENVPACKAGE_KEY_DIR
* SHATHEL_ENVPACKAGE_WORKING_DIR

## Ansible - available variables
* ANSIBLE_BECOME_PASS
* ANSIBLE_SSH_ARGS
* ANSIBLE_HOST_KEY_CHECKING
* ANSIBLE_NOCOWS
* ANSIBLE_RETRY_FILES_SAVE_PATH
* ansible-extra-vars.json <- all env vars (with _ insted of . to lower case)

## PACKER
* PACKER_CACHE_DIR
* PACKER_LOG      
* PACKER_LOG_PATH 
* PACKER_NO_COLOR 
* packer-extra-vars.json <- all env vars (with _ insted of . to lower case)

## TERRAFORM
* TF_INPUT   
* TF_LOG     
* TF_LOG_PATH

## VAGRANT
* VAGRANT_DOTFILE_PATH
* VAGRANT_VAGRANTFILE
* VAGRANT_HOME