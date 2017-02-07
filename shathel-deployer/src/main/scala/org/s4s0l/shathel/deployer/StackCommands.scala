package org.s4s0l.shathel.deployer

import java.io.File
import java.lang.Boolean
import java.util

import org.s4s0l.shathel.commons.core.{Stack, StackOperations}
import org.s4s0l.shathel.commons.core.environment.Environment
import org.s4s0l.shathel.commons.core.model.GavUtils
import org.s4s0l.shathel.commons.core.provision.StackCommand
import org.s4s0l.shathel.commons.core.stack.StackReference
import org.s4s0l.shathel.deployer.shell.customization.CustomBanner
import org.springframework.shell.core.annotation.{CliCommand, CliOption}

import scala.collection.JavaConverters._

/**
  * @author Matcin Wielgus
  */
class StackCommands(parametersCommands: ParametersCommands, environmentCommands: EnvironmentCommands)
  extends ShathelCommands(parametersCommands) {


  @CliCommand(value = Array("stack start"), help = "Displays what will be done with given stack.")
  def load(
            @CliOption(key = Array("name"), mandatory = true, help = "Package name in form of group:name:version to be run")
            name: String,
            @CliOption(key = Array("inspect"), mandatory = false, help = "If true will only inspect not run",
              specifiedDefaultValue = "true", unspecifiedDefaultValue = "true")
            inspect: Boolean,
            @CliOption(key = Array("inspect-compose"), mandatory = false, help = "If true will output compose files used",
              specifiedDefaultValue = "false", unspecifiedDefaultValue = "false")
            inspectLong: Boolean,
            @CliOption(key = Array("env-name"), mandatory = false, help = "see environment use command for details")
            environment: String,
            @CliOption(key = Array("storage-file"), mandatory = false, help = "see storage open command for details")
            file: File,
            @CliOption(key = Array("storage-init"), mandatory = false, help = "see storage open command for details")
            initIfAbsent: java.lang.Boolean,
            @CliOption(key = Array("params"), mandatory = false, help = "see parameters add command for details")
            map: java.util.Map[String, String]
          ): String = {
    runCommand(name, inspect, inspectLong, environment, file, initIfAbsent, map)((s) => s.createStartCommand())
  }

  @CliCommand(value = Array("stack stop"), help = "Displays what will be done with given stack.")
  def load(
            @CliOption(key = Array("name"), mandatory = true, help = "Package name in form of group:name:version to be run")
            name: String,
            @CliOption(key = Array("inspect"), mandatory = false, help = "If true will only inspect not run",
              specifiedDefaultValue = "true", unspecifiedDefaultValue = "true")
            inspect: Boolean,
            @CliOption(key = Array("inspect-compose"), mandatory = false, help = "If true will output compose files used",
              specifiedDefaultValue = "false", unspecifiedDefaultValue = "false")
            inspectLong: Boolean,
            @CliOption(key = Array("with-dependencies"), mandatory = false, help = "If true will stop all dependencies also",
              specifiedDefaultValue = "false", unspecifiedDefaultValue = "false")
            withDependencies: Boolean,
            @CliOption(key = Array("env-name"), mandatory = false, help = "see environment use command for details")
            environment: String,
            @CliOption(key = Array("storage-file"), mandatory = false, help = "see storage open command for details")
            file: File,
            @CliOption(key = Array("storage-init"), mandatory = false, help = "see storage open command for details")
            initIfAbsent: java.lang.Boolean,
            @CliOption(key = Array("params"), mandatory = false, help = "see parameters add command for details")
            map: java.util.Map[String, String]
          ): String = {
    runCommand(name, inspect, inspectLong, environment, file, initIfAbsent, map)((s) => s.createStopCommand(withDependencies))
  }


  private def runCommand(name: String, inspect: Boolean, inspectLong: Boolean, environment: String, file: File, initIfAbsent: Boolean, map: util.Map[String, String])
                        (factory: (Stack) => StackOperations): String = {
    shathel(map, builder()
      .environment(environment)
      .storageFile(file)
      .storageInit(initIfAbsent)
    )(context => {
      val (storage, solution, environment) = environmentCommands.getEnvironment(context)
      val openStack = solution.openStack(environment, getStackReference(name))
      val command = factory(openStack)

      val output = this.inspect(command, inspectLong)
      if (!inspect) {
        openStack.run(command)
      }
      return response(output)
    })
  }

  private def getStackReference(name: String) = {
    val groupNoVersion = "org.s4s0l.shathel:([a-zA-Z-0-9\\.]+)".r
    val noGroupNoVersion = "([a-zA-Z-0-9\\.]+)".r
    new StackReference(name match {
      case groupNoVersion(_) => s"X${name}:${CustomBanner.versionInfo()}"
      case noGroupNoVersion(_) => s"${name}:${CustomBanner.versionInfo()}"
      case _ => name
    })
  }

  private def inspect(command: StackOperations, inspectLong: Boolean): Map[String, AnyRef] = {
    def arrayToMap = collection.breakOut[Seq[StackCommand], (String, util.Map[String, AnyRef]), Map[String, util.Map[String, AnyRef]]]

    return command.getCommands.asScala.map((elem) => {
      elem.getDescription.getDeployName -> Map(
        "gav" -> elem.getDescription.getGav,
        "type" -> elem.getType.name(),
        "provisioners" -> elem.getProvisioners.asScala.map((p) => s"${p.name}:${p.`type`}").asJava,
        "compose" -> (if (inspectLong) elem.getMutableModel.getParsedYml else "<hidden>")
      ).asJava
    })(arrayToMap)
  }
}

