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
  * @author Matcin Wielgus
  */
class EnvironmentCommands(parametersCommands: ParametersCommands, storageCommands: StorageCommands)
  extends ShathelCommands(parametersCommands) {

  @CliCommand(value = Array("environment use"), help = "Sets parameters for environment to use later on")
  def use(
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
      null;
    })

  }


  @CliCommand(value = Array("environment init"), help = "Performs initialization on ")
  def init(
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
      val (_,_, environment) = getEnvironment(context)
      environment.initialize()
      ok()
    })

  }

  @CliCommand(value = Array("environment inspect"), help = "Performs initialization of environment")
  def inspect(
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
      val (_,_, environment) = getEnvironment(context)
      response(Map(
        "initialized" -> Try(environment.isInitialized).getOrElse(false),
        "started" -> Try(environment.isStarted).getOrElse(false),
        "verified" -> Try {
          environment.verify();
          true
        }.getOrElse(false)))
    })
  }


  @CliCommand(value = Array("environment start"), help = "Performs start of environment")
  def start(
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
      val (_,_, environment) = getEnvironment(context)
      environment.start()
      ok()
    })

  }

  @CliCommand(value = Array("environment stop"), help = "Performs stop of environment")
  def stop(
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
      val (_,_, environment) = getEnvironment(context)
      environment.stop()
      ok()
    })
  }


  @CliCommand(value = Array("environment destroy"), help = "Performs removal of all docker-machines.")
  def destroy(
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
      val (_,_, environment) = getEnvironment(context)
      environment.destroy()
      ok()
    })
  }

  @CliCommand(value = Array("environment save"), help = "Dumps docker machine settings to encrypted file.")
  def save(
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
      val (_,_, environment) = getEnvironment(context)
      environment.save()
      ok()
    })
  }


  @CliCommand(value = Array("environment load"), help = "Loads docker machine settings from encrypted file.")
  def load(
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
      val (_,_, environment) = getEnvironment(context)
      environment.load()
      ok()
    })
  }

  def getEnvironment(context: ShathelCommandContext): (Storage, Solution, Environment) = {
    val storage = storageCommands.getStorage(context)
    val solution = context.shathel.getSolution(storage)
    val environment = solution.getEnvironment(context.environment())
    (storage,solution, environment)
  }
}
