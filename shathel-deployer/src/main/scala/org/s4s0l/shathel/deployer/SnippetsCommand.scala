package org.s4s0l.shathel.deployer

import java.io.File

import org.springframework.shell.core.annotation.{CliCommand, CliOption}

/**
  * @author Matcin Wielgus
  */
class SnippetsCommand(parametersCommands: ParametersCommands, storageCommands: EnvironmentCommands)
  extends ShathelCommands(parametersCommands) {


  @CliCommand(value = Array("snippet registry"), help = "Shows commands to setup local registry access")
  def registry(
           @CliOption(key = Array("env-name"), mandatory = false, help = "Environment name to use")
           environment: String,
           @CliOption(key = Array("storage-file"), mandatory = false, help = "see storage open command for details")
           file: File,
           @CliOption(key = Array("storage-init"), mandatory = false, help = "see storage open command for details")
           initIfAbsent: java.lang.Boolean,
           @CliOption(key = Array("params"), mandatory = false, help = "see parameters add command for details")
           map: java.util.Map[String, String]
         ): String = {
    shathel(map, builder()
      .environment(environment)
      .storageFile(file)
      .storageInit(initIfAbsent)
    )(context => {
      val env = storageCommands.getEnvironment(context)

      def settingsDir = env._3.getEnvironmentContext.getSettingsDirectory
      def managerIp = env._3.getEnvironmentApiFacade.getIpForManagementNode

      def ret =
        s"""
          |sudo mkdir -p /etc/docker/certs.d/${managerIp}:4000/
          |sudo mkdir -p /etc/docker/certs.d/${managerIp}:4001/
          |sudo cp ${new File(settingsDir, "registries/certs/ca.crt").getAbsolutePath} /etc/docker/certs.d/${managerIp}:4000/ca.crt
          |sudo cp ${new File(settingsDir, "registries/mirrorcerts/ca.crt").getAbsolutePath} /etc/docker/certs.d/${managerIp}:4001/ca.crt
        """.stripMargin

      return ret
    })

  }


  @CliCommand(value = Array("snippet usemachine"), help = "Shows commands to set docker envs")
  def usemachine(
           @CliOption(key = Array("env-name"), mandatory = false, help = "Environment name to use")
           environment: String,
           @CliOption(key = Array("storage-file"), mandatory = false, help = "see storage open command for details")
           file: File,
           @CliOption(key = Array("storage-init"), mandatory = false, help = "see storage open command for details")
           initIfAbsent: java.lang.Boolean,
           @CliOption(key = Array("params"), mandatory = false, help = "see parameters add command for details")
           map: java.util.Map[String, String]
         ): String = {
    shathel(map, builder()
      .environment(environment)
      .storageFile(file)
      .storageInit(initIfAbsent)
    )(context => {
      val env = storageCommands.getEnvironment(context)

      def settingsDir = env._3.getEnvironmentContext.getSettingsDirectory
      def managerIp = env._3.getEnvironmentApiFacade.getIpForManagementNode
      def xx = env._3.getEnvironmentContext.getContextName
      def ret =
        s"""
           |eval $$(docker-machine -s ${settingsDir.getAbsolutePath} env ${xx}-manager-1)
           |eval $$(docker-machine -s ${settingsDir.getAbsolutePath} env ${xx}-manager-2)
           |eval $$(docker-machine -s ${settingsDir.getAbsolutePath} env ${xx}-worker-2)
        """.stripMargin

      return ret
    })

  }

}
