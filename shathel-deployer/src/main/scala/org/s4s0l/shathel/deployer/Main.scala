package org.s4s0l.shathel.deployer

import org.s4s0l.shathel.deployer.shell.SpringShellApplication
import org.slf4j.{Logger, LoggerFactory}
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.{Bean, ComponentScan}

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
  def SampleCommand: SampleCommand = {
    return new SampleCommand
  }

  @Bean
  def mapConverter: MapConverter = {
    return new MapConverter
  }
}
