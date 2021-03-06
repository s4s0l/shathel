package org.s4s0l.shathel.deployer

import java.lang.Boolean
import java.util

import org.s4s0l.shathel.commons.core.dependencies.StackLocator
import org.s4s0l.shathel.commons.core.environment.Environment
import org.s4s0l.shathel.commons.core.stack.StackProvisionerDefinition
import org.s4s0l.shathel.commons.core.{StackOperations, Stacks}
import org.s4s0l.shathel.commons.scripts.NamedExecutable
import org.slf4j.{Logger, LoggerFactory}
import org.springframework.shell.core.annotation.{CliCommand, CliOption}

import scala.collection.JavaConverters._
import scala.collection.mutable

/**
  * @author Marcin Wielgus
  */
class StackCommands(parametersCommands: ParametersCommands, environmentCommands: EnvironmentCommands)
  extends ShathelCommands(parametersCommands) {
  val LOGGER: Logger = LoggerFactory.getLogger(classOf[StackCommands])

  @CliCommand(value = Array("stack start"), help = "Displays what will be done with given stack.")
  def start(
             @CliOption(key = Array("name", ""), mandatory = true, help = "Package name in form of group:name:version to be run, may be comma separated list")
             name: String,
             @CliOption(key = Array("inspect"), mandatory = false, help = "If true will only inspect not run",
               specifiedDefaultValue = "true", unspecifiedDefaultValue = "false")
             inspect: Boolean,
             @CliOption(key = Array("inspect-compose"), mandatory = false, help = "If true will output compose files used",
               specifiedDefaultValue = "true", unspecifiedDefaultValue = "false")
             inspectLong: Boolean,
             @CliOption(key = Array("with-optional"), mandatory = false, help = "If true will start with optional dependencies",
               specifiedDefaultValue = "true", unspecifiedDefaultValue = "false")
             withOptional: Boolean,
             @CliOption(key = Array("params"), mandatory = false, help = "see parameters add command for details")
             map: java.util.Map[String, String]
           ): String = {
    runCommand(name, inspect, inspectLong, map)((s, c) => {
      s.createStartCommand(withOptional, c)
    })
  }

  @CliCommand(value = Array("stack ls"), help = "Displays what will be done with given stack.")
  def ls(
          @CliOption(key = Array("params"), mandatory = false, help = "see parameters add command for details")
          map: java.util.Map[String, String]
        ): String = {
    shathel(map, builder())(
      context => {
        val (storage, solution, environment) = environmentCommands.getEnvironment(context)
        val stacks = environment.getIntrospectionProvider.getAllStacks.getStacks.asScala
        val ret: Map[String, AnyRef] = stacks.map(x =>
          x.getReference.getGav -> x.getServices.asScala.map(s =>
            s.getServiceName -> s"${s.getCurrentInstances}/${s.getRequiredInstances}").toMap.asJava).toMap
        return response(ret)
      })
  }

  @CliCommand(value = Array("stack purge"), help = "Removes all stacks!")
  def purge(
             @CliOption(key = Array("inspect"), mandatory = false, help = "If true will only inspect not run",
               specifiedDefaultValue = "true", unspecifiedDefaultValue = "false")
             inspect: Boolean,
             @CliOption(key = Array("inspect-compose"), mandatory = false, help = "If true will output compose files used",
               specifiedDefaultValue = "true", unspecifiedDefaultValue = "false")
             inspectLong: Boolean,
             @CliOption(key = Array("params"), mandatory = false, help = "see parameters add command for details")
             map: java.util.Map[String, String]
           ): String = {
    shathel(map, builder())(
      context => {
        val (storage, solution, environment) = environmentCommands.getEnvironment(context)
        val command = solution.getPurgeCommand(environment)

        if (inspect) {
          return response(this.inspect(command, inspectLong))
        } else {
          try {
            solution.run(command)
            return ok();
          } catch {
            case e: Exception =>
              LOGGER.warn(response(this.inspect(command, true)))
              throw e
          }
        }
      })
  }

  private def inspect(command: StackOperations, inspectLong: Boolean)
  : mutable.Seq[util.Map[String, AnyRef]] = {
    //    def arrayToMap = collection.breakOut[Seq[StackCommand], (String, util.Map[String, AnyRef]), Map[String, util.Map[String, AnyRef]]]

    def display = (p: NamedExecutable) => p.getName

    def displayProv = (p: StackProvisionerDefinition) => p.getScriptName

    def ret = command.getCommands.asScala.map((elem) => {
      Map(
        "type" -> elem.getType.name(),
        "gav" -> elem.getDescription.getGav,
        "compose" -> (if (inspectLong) elem.getComposeModel.getParsedYml else "<hidden>"),
        "pre-provisioners" -> elem.getDescription.getPreProvisioners.asScala.map(displayProv).asJava,
        "enriching-provisioners" -> elem.getEnricherPreProvisioners.asScala.map(display).asJava,
        "post-provisioners" -> elem.getDescription.getPostProvisioners.asScala.map(displayProv).asJava
      ).asJava
    })

    ret
  }

  @CliCommand(value = Array("stack stop"), help = "Displays what will be done with given stack.")
  def stop(
            @CliOption(key = Array("name", ""), mandatory = true, help = "Package name in form of group:name:version to be run, may be comma separated list")
            name: String,
            @CliOption(key = Array("inspect"), mandatory = false, help = "If true will only inspect not run",
              specifiedDefaultValue = "true", unspecifiedDefaultValue = "false")
            inspect: Boolean,
            @CliOption(key = Array("inspect-compose"), mandatory = false, help = "If true will output compose files used",
              specifiedDefaultValue = "true", unspecifiedDefaultValue = "false")
            inspectLong: Boolean,
            @CliOption(key = Array("with-dependencies"), mandatory = false, help = "If true will stop all dependencies also",
              specifiedDefaultValue = "true", unspecifiedDefaultValue = "false")
            withDependencies: Boolean,
            @CliOption(key = Array("with-optional"), mandatory = false, help = "If true will stop optional dependencies",
              specifiedDefaultValue = "true", unspecifiedDefaultValue = "false")
            withOptional: Boolean,
            @CliOption(key = Array("params"), mandatory = false, help = "see parameters add command for details")
            map: java.util.Map[String, String]
          ): String = {
    runCommand(name, inspect, inspectLong, map)((s, c) => s.createStopCommand(withDependencies, withOptional, c))
  }

  private def runCommand(name: String, inspect: Boolean, inspectLong: Boolean,
                         map: util.Map[String, String])
                        (factory: (Stacks, Environment) => StackOperations): String = {
    shathel(map, builder())(
      context => {
        val (storage, solution, environment) = environmentCommands.getEnvironment(context)
        val locators = name.split(",").toList.map(new StackLocator(_))
        val openStack = solution.openStack(locators.asJava)
        val command = factory(openStack, environment)

        if (inspect) {
          return response(this.inspect(command, inspectLong))
        } else {
          try {
            solution.run(command)
            return ok();
          } catch {
            case e: Exception =>
              LOGGER.warn(response(this.inspect(command, true)))
              throw e
          }
        }

      })
  }
}

