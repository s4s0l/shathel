package org.s4s0l.shathel.deployer

import java.io.File
import java.lang.Boolean
import java.util

import org.s4s0l.shathel.commons.core.{Stack, StackOperations}
import org.s4s0l.shathel.commons.core.environment.{Environment, StackCommand}
import org.s4s0l.shathel.commons.core.model.GavUtils
import org.s4s0l.shathel.commons.core.stack.{StackProvisionerDefinition, StackReference}
import org.s4s0l.shathel.deployer.shell.customization.CustomBanner
import org.slf4j.LoggerFactory
import org.springframework.shell.core.annotation.{CliCommand, CliOption}

import scala.collection.JavaConverters._

/**
  * @author Matcin Wielgus
  */
class StackCommands(parametersCommands: ParametersCommands, environmentCommands: EnvironmentCommands)
  extends ShathelCommands(parametersCommands) {
  val LOGGER = LoggerFactory.getLogger(classOf[StackCommands]);

  @CliCommand(value = Array("stack start"), help = "Displays what will be done with given stack.")
  def start(
             @CliOption(key = Array("name", ""), mandatory = true, help = "Package name in form of group:name:version to be run")
             name: String,
             @CliOption(key = Array("inspect"), mandatory = false, help = "If true will only inspect not run",
               specifiedDefaultValue = "false", unspecifiedDefaultValue = "false")
             inspect: Boolean,
             @CliOption(key = Array("inspect-compose"), mandatory = false, help = "If true will output compose files used",
               specifiedDefaultValue = "true", unspecifiedDefaultValue = "true")
             inspectLong: Boolean,
             @CliOption(key = Array("params"), mandatory = false, help = "see parameters add command for details")
             map: java.util.Map[String, String]
           ): String = {
    runCommand(name, inspect, inspectLong, map)((s, c) => {
      s.createStartCommand()
    })
  }

  @CliCommand(value = Array("stack stop"), help = "Displays what will be done with given stack.")
  def stop(
            @CliOption(key = Array("name", ""), mandatory = true, help = "Package name in form of group:name:version to be run")
            name: String,
            @CliOption(key = Array("inspect"), mandatory = false, help = "If true will only inspect not run",
              specifiedDefaultValue = "false", unspecifiedDefaultValue = "false")
            inspect: Boolean,
            @CliOption(key = Array("inspect-compose"), mandatory = false, help = "If true will output compose files used",
              specifiedDefaultValue = "true", unspecifiedDefaultValue = "true")
            inspectLong: Boolean,
            @CliOption(key = Array("with-dependencies"), mandatory = false, help = "If true will stop all dependencies also",
              specifiedDefaultValue = "false", unspecifiedDefaultValue = "false")
            withDependencies: Boolean,
            @CliOption(key = Array("params"), mandatory = false, help = "see parameters add command for details")
            map: java.util.Map[String, String]
          ): String = {
    runCommand(name, inspect, inspectLong, map)((s, c) => s.createStopCommand(withDependencies))
  }


  private def runCommand(name: String, inspect: Boolean, inspectLong: Boolean,
                         map: util.Map[String, String])
                        (factory: (Stack, DeployerParameters.ShathelCommandContext) => StackOperations): String = {
    shathel(map, builder())(
      context => {
        val (storage, solution, environment) = environmentCommands.getEnvironment(context)
        val openStack = solution.openStack(environment, getStackReference(name))
        val command = factory(openStack, context)

        val output = this.inspect(command, inspectLong)
        if (!inspect) {
          try {
            openStack.run(command)
          } catch {
            case e: Exception => {
              LOGGER.warn(response(output))
              throw e
            }
          }
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

    def display = (p: Object) => s"${p.getClass.getSimpleName}:${p.toString}"

    def displayProv = (p: StackProvisionerDefinition) => s"${p.getName}:${p.getType}"

    return command.getCommands.asScala.map((elem) => {
      elem.getDescription.getDeployName -> Map(
        "gav" -> elem.getDescription.getGav,
        "type" -> elem.getType.name(),
        "pre-provisioners" -> elem.getDescription.getPreProvisioners.asScala.map(displayProv).asJava,
        "enriching-provisioners" -> elem.getEnricherPreProvisioners.asScala.map(display).asJava,
        "post-provisioners" -> elem.getDescription.getPostProvisioners.asScala.map(displayProv).asJava,
        "compose" -> (if (inspectLong) elem.getComposeModel.getParsedYml else "<hidden>")
      ).asJava
    })(arrayToMap)
  }
}

