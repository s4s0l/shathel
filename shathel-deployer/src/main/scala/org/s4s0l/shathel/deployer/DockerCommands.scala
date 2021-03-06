package org.s4s0l.shathel.deployer

import java.io.File

import org.s4s0l.shathel.commons.docker.DockerMachineWrapper
import org.springframework.shell.core.annotation.{CliCommand, CliOption}

/**
  * @author Marcin Wielgus
  */
class DockerCommands(parametersCommands: ParametersCommands, environmentCommands: EnvironmentCommands)
  extends ShathelCommands(parametersCommands) {
  
  @CliCommand(value = Array("docker"), help = "Runs docker command on manager node")
  def docker(
              @CliOption(key = Array(""), mandatory = false, help = "command to execute")
              command: String,
              @CliOption(key = Array("env-name"), mandatory = false, help = "Environment name to use")
              environment: String,
              @CliOption(key = Array("storage-file"), mandatory = false, help = "see storage open command for details")
              file: File,
              @CliOption(key = Array("params"), mandatory = false, help = "see parameters add command for details")
              map: java.util.Map[String, String]
            ): String = {
    shathel(map, builder()
      .environment(environment)
      .storageFile(file)
    )(context => {
      val (storage, solution, environment) = environmentCommands.getEnvironment(context)
      val file1 = storage.getSettingsDirectory(context, context.environment())
      environment.getEnvironmentApiFacade.getManagerNodeWrapper.getExec.executeForOutput(command)

    })
  }


}
