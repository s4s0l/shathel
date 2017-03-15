package org.s4s0l.shathel.deployer

import java.io.File

import org.springframework.shell.core.annotation.{CliCommand, CliOption}

/**
  * @author Marcin Wielgus
  */
class SecretsCommand(parametersCommands: ParametersCommands, storageCommands: EnvironmentCommands)
  extends ShathelCommands(parametersCommands) {


  @CliCommand(value = Array("secret update"), help = "Updates secret to new version and updates all services using it")
  def update(
              @CliOption(key = Array("", "name"), mandatory = true, help = "secret name")
              name: String,
              @CliOption(key = Array("file"), mandatory = false, help = "file containing secret")
              file: File,
              @CliOption(key = Array("params"), mandatory = false, help = "see parameters add command for details")
              map: java.util.Map[String, String]
            ): String = {
    shathel(map, builder())(
      context => {
        val env = storageCommands.getEnvironment(context)
        val manager = env._3.getEnvironmentApiFacade.getSecretManager
        return manager.secretUpdate(name, file)
      })

  }

  @CliCommand(value = Array("secret current"), help = "Shows current secret name")
  def current(
               @CliOption(key = Array("", "name"), mandatory = true, help = "secret name")
               name: String,
               @CliOption(key = Array("params"), mandatory = false, help = "see parameters add command for details")
               map: java.util.Map[String, String]
             ): String = {
    shathel(map, builder())(
      context => {
        val env = storageCommands.getEnvironment(context)
        val manager = env._3.getEnvironmentApiFacade.getSecretManager
        return manager.secretCurrentName(name)
      })

  }

  @CliCommand(value = Array("secret versions"), help = "Shows all versions of secret")
  def versions(
                @CliOption(key = Array("", "name"), mandatory = true, help = "secret name")
                name: String,
                @CliOption(key = Array("params"), mandatory = false, help = "see parameters add command for details")
                map: java.util.Map[String, String]
              ): String = {
    shathel(map, builder())(
      context => {
        val env = storageCommands.getEnvironment(context)
        val manager = env._3.getEnvironmentApiFacade.getSecretManager
        return response(manager.getAllSecretNames(name))
      })

  }


  @CliCommand(value = Array("secret services"), help = "Shows all services using secret")
  def services(
                @CliOption(key = Array("", "name"), mandatory = true, help = "secret name")
                name: String,
                @CliOption(key = Array("params"), mandatory = false, help = "see parameters add command for details")
                map: java.util.Map[String, String]
              ): String = {
    shathel(map, builder())(
      context => {
        val env = storageCommands.getEnvironment(context)
        val manager = env._3.getEnvironmentApiFacade.getSecretManager
        return response(manager.getServicesUsingSecret(name))
      })

  }


  @CliCommand(value = Array("secret ls"), help = "Shows all secrets")
  def ls(
          @CliOption(key = Array("params"), mandatory = false, help = "see parameters add command for details")
          map: java.util.Map[String, String]
        ): String = {
    shathel(map, builder())(
      context => {
        val env = storageCommands.getEnvironment(context)
        val manager = env._3.getEnvironmentApiFacade.getSecretManager
        return response(manager.getAllSecretNames)
      })

  }

}
