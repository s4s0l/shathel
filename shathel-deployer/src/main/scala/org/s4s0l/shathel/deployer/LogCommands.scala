package org.s4s0l.shathel.deployer

import java.io.File

import org.slf4j.{Logger, LoggerFactory}
import org.springframework.shell.core.CommandMarker
import org.springframework.shell.core.annotation.{CliCommand, CliOption}

/**
  * @author Marcin Wielgus
  */
class LogCommands extends CommandMarker with OutputFormatter {


  @CliCommand(value = Array("debug"), help = "Debug logging.")
  def debug(): String = {
    return response(Map(
      logLevel(Logger.ROOT_LOGGER_NAME, "INFO"),
      logLevel("org.s4s0l.shathel", "DEBUG")
    ))
  }

  @CliCommand(value = Array("info"), help = "Info logging.")
  def info(): String = {
    return response(Map(
      logLevel(Logger.ROOT_LOGGER_NAME, "ERROR"),
      logLevel("org.s4s0l.shathel", "INFO")
    ))
  }

  @CliCommand(value = Array("trace"), help = "Trace logging.")
  def trace(): String = {
    return response(Map(
      logLevel(Logger.ROOT_LOGGER_NAME, "DEBUG"),
      logLevel("org.s4s0l.shathel", "TRACE")
    ))
  }

  @CliCommand(value = Array("log"), help = "Changes log level.")
  def log(
           @CliOption(key = Array("level"), mandatory = true, help = "level to set") level: String,
           @CliOption(key = Array("logger"), mandatory = false, unspecifiedDefaultValue = Logger.ROOT_LOGGER_NAME, specifiedDefaultValue = Logger.ROOT_LOGGER_NAME, help = "logger name") logger: String
         ): String = {

    return response(Map(logLevel(logger,level)))
  }

  def logLevel( logger: String,level: String): (String, String) = {
    val lev = ch.qos.logback.classic.Level.toLevel(level);
    LoggerFactory.getLogger(logger).asInstanceOf[ch.qos.logback.classic.Logger].setLevel(lev);
    return logger -> lev.levelStr
  }
}

