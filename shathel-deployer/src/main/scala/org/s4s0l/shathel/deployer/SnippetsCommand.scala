package org.s4s0l.shathel.deployer

import java.io.File

import org.springframework.shell.core.annotation.{CliCommand, CliOption}
import scala.collection.JavaConverters._

/**
  * @author Marcin Wielgus
  */
@Deprecated
class SnippetsCommand(parametersCommands: ParametersCommands, storageCommands: EnvironmentCommands)
  extends ShathelCommands(parametersCommands) {


  @Deprecated
  @CliCommand(value = Array("snippet registry"), help = "Shows commands to setup local registry access")
  def registry(
                @CliOption(key = Array("params"), mandatory = false, help = "see parameters add command for details")
                map: java.util.Map[String, String]
              ): String = {
    shathel(map, builder())(
      context => {
        val env = storageCommands.getEnvironment(context)

        def settingsDir = env._3.getEnvironmentContext.getSettingsDirectory

        def managerIp = env._3.getEnvironmentApiFacade.getNodes.asScala.find(_.getRole == "manager").map(_.getPublicIp)

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


  @Deprecated
  @CliCommand(value = Array("snippet usemachine"), help = "Shows commands to set docker envs")
  def usemachine(
                  @CliOption(key = Array("params"), mandatory = false, help = "see parameters add command for details")
                  map: java.util.Map[String, String]
                ): String = {
    shathel(map, builder())(
      context => {
        val env = storageCommands.getEnvironment(context)

        def settingsDir = env._3.getEnvironmentContext.getSettingsDirectory

        def managerIp = "donotuse"

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
