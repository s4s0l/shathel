package org.s4s0l.shathel.deployer

import java.io.{PrintWriter, StringWriter}
import java.util.logging.{Formatter, Handler, LogManager, LogRecord}
import javax.annotation.PostConstruct

import org.apache.commons.io.IOUtils
import org.s4s0l.shathel.deployer.shell.SpringShellApplication
import org.s4s0l.shathel.deployer.shell.customization.MapConverter
import org.slf4j.{Logger, LoggerFactory}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.{Bean, ComponentScan}
import org.springframework.core.annotation.Order
import org.springframework.shell.core.{JLineLogHandler, SimpleExecutionStrategy}
import org.springframework.shell.support.logging.HandlerUtils
import org.springframework.shell.support.util.OsUtils

object Main {
  def main(args: Array[String]) {
    val exitCode: Int = SpringShellApplication.run(Array(classOf[Main].asInstanceOf[AnyRef]), args)
    System.exit(exitCode)
  }

  private val LOGGER: Logger = LoggerFactory.getLogger(classOf[Main])
}




@SpringBootApplication
@ComponentScan(Array("org.s4s0l.shathel.deployer.shell.customization"))
class Main {


  @Bean
  def ParametersCommands: ParametersCommands = new ParametersCommands

  @Bean
  def StorageCommands: StorageCommands = new StorageCommands(ParametersCommands)

  @Bean
  def EnvironmentCommands: EnvironmentCommands = new EnvironmentCommands(ParametersCommands, StorageCommands)

  @Bean
  def StackCommands: StackCommands = new StackCommands(ParametersCommands, EnvironmentCommands)

  @Bean
  def DockerCommands: DockerCommands = new DockerCommands(ParametersCommands, EnvironmentCommands)

  @Bean
  def WorkingDirectoryCommand: WorkingDirectoryCommand = new WorkingDirectoryCommand

  @Bean
  def PromptProvider: PromptProvider = new PromptProvider(ParametersCommands)

  @Bean
  def SnippetsCommand: SnippetsCommand = new SnippetsCommand(ParametersCommands, EnvironmentCommands)

  @Bean
  def SecretsCommand: SecretsCommand = new SecretsCommand(ParametersCommands, EnvironmentCommands)

  @Bean
  def mapConverter: MapConverter = {
    return new MapConverter
  }
}
