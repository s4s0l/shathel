# Configuration concepts

# Settings
## Global settings
* shathel.env
* shathel.dir
* shathel.deployer.historyfile

## Solution level settings
* shathel.solution.name
* shathel.solution.file_default_version
* shathel.solution.file_default_group
* shathel.solution.file_base_dir
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

## Environment level settings
* shathel.env.{env}.net
* shathel.env.{env}.init
* shathel.env.{env}.registry
* shathel.env.{env}.managers
* shathel.env.{env}.forceful
* shathel.env.{env}.workers
* shathel.env.{env}.dependenciesDir
* shathel.env.{env}.dataDir
* shathel.env.{env}.safeDir
* shathel.env.{env}.settingsDir
* shathel.env.{env}.enrichedDir
* shathel.env.{env}.tempDir
* shathel.env.{env}.pull
* shathel.env.{env}.domain
* shathel.env.{env}.useglobalvagrant

## Passwords settings
* shathel.env.{env}.safePassword
* shathel.env.{env}.{secretName}_secret_path
* shathel.env.{env}.{secretName}_secret_path

## Gradle tests variables
* shathel.plugin.ip
* shathel.plugin.{stackName}.{serviceName}.{targetPort}
* shathel.plugin.local.override.mappings
* shathel.plugin.local.override.current
* shathel.plugin.current.gav
* shathel.plugin.current