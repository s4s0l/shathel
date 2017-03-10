package org.s4s0l.shathel.deployer

import java.io.File

import org.s4s0l.shathel.commons.core.Solution
import org.s4s0l.shathel.commons.core.environment.Environment
import org.s4s0l.shathel.commons.core.storage.Storage
import org.s4s0l.shathel.deployer.DeployerParameters.ShathelCommandContext
import org.springframework.shell.core.annotation.{CliCommand, CliOption}
import org.yaml.snakeyaml.Yaml

import scala.collection.JavaConverters._
import scala.util.Try

/**
  * @author Marcin Wielgus
  */
class EnvironmentCommands(parametersCommands: ParametersCommands, storageCommands: StorageCommands)
  extends ShathelCommands(parametersCommands) {


  @CliCommand(value = Array("environment use"), help = "Sets current env")
  def init(
            @CliOption(key = Array(""), mandatory = false, help = "Environment name to use")
            environment: String
          ): String = {
    shathel(builder().environment(environment).build().asJava, builder())(
      context => {
        val (_, _, environment) = getEnvironment(context)
        inspectResult(environment)
      })

  }


  @CliCommand(value = Array("environment init"), help = "Performs initialization on ")
  def init(
            @CliOption(key = Array("params"), mandatory = false, help = "see parameters add command for details")
            map: java.util.Map[String, String]
          ): String = {
    shathel(map, builder())(
      context => {
        val (_, _, environment) = getEnvironment(context)
        environment.initialize()
        ok()
      })

  }

  @CliCommand(value = Array("environment inspect"), help = "Performs initialization of environment")
  def inspect(
               @CliOption(key = Array("params"), mandatory = false, help = "see parameters add command for details")
               map: java.util.Map[String, String]
             ): String = {
    shathel(map, builder())(
      context => {
        val (_, _, environment) = getEnvironment(context)
        inspectResult(environment)
      })
  }


  private def inspectResult(environment: Environment) = {
    response(Map(
      "initialized" -> Try(environment.isInitialized).getOrElse(false),
      "started" -> Try(environment.isStarted).getOrElse(false),
      "verified" -> Try {
        environment.verify();
        true
      }.getOrElse(false)))
  }

  @CliCommand(value = Array("environment start"), help = "Performs start of environment")
  def start(
             @CliOption(key = Array("params"), mandatory = false, help = "see parameters add command for details")
             map: java.util.Map[String, String]
           ): String = {
    shathel(map, builder())(
      context => {
        val (_, _, environment) = getEnvironment(context)
        environment.start()
        ok()
      })

  }

  @CliCommand(value = Array("environment stop"), help = "Performs stop of environment")
  def stop(
            @CliOption(key = Array("params"), mandatory = false, help = "see parameters add command for details")
            map: java.util.Map[String, String]
          ): String = {
    shathel(map, builder())(
      context => {
        val (_, _, environment) = getEnvironment(context)
        environment.stop()
        ok()
      })
  }


  @CliCommand(value = Array("environment destroy"), help = "Performs removal of all docker-machines.")
  def destroy(
               @CliOption(key = Array("params"), mandatory = false, help = "see parameters add command for details")
               map: java.util.Map[String, String]
             ): String = {
    shathel(map, builder())(
      context => {
        val (_, _, environment) = getEnvironment(context)
        environment.destroy()
        ok()
      })
  }

  @CliCommand(value = Array("environment save"), help = "Dumps docker machine settings to encrypted file.")
  def save(
            @CliOption(key = Array("params"), mandatory = false, help = "see parameters add command for details")
            map: java.util.Map[String, String]
          ): String = {
    shathel(map, builder())(
      context => {
        val (_, _, environment) = getEnvironment(context)
        environment.save()
        ok()
      })
  }


  @CliCommand(value = Array("environment load"), help = "Loads docker machine settings from encrypted file.")
  def load(
            @CliOption(key = Array("params"), mandatory = false, help = "see parameters add command for details")
            map: java.util.Map[String, String]
          ): String = {
    shathel(map, builder())(
      context => {
        val (_, _, environment) = getEnvironment(context)
        environment.load()
        ok()
      })
  }

  def getEnvironment(context: ShathelCommandContext): (Storage, Solution, Environment) = {
    val storage = storageCommands.getStorage(context)
    val solution = context.shathel.getSolution(storage)
    val environment = solution.getEnvironment(context.environment())
    (storage, solution, environment)
  }
}
