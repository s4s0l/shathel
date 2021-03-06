package org.s4s0l.shathel.deployer

import java.util

import org.s4s0l.shathel.commons.core.{CommonParams, MapParameters, ParameterProvider, Parameters}
import org.s4s0l.shathel.deployer.shell.customization.MapConverterKeysProvider
import org.springframework.shell.core.CommandMarker
import org.springframework.shell.core.annotation.{CliCommand, CliOption}

import scala.collection.JavaConverters._


/**
  * @author Marcin Wielgus
  */
class ParametersCommands extends CommandMarker with ParametersKeyProvider with OutputFormatter {
  private var parameters: Parameters = Parameters.fromMapWithSysPropAndEnv(Map[String, String]().asJava)
  private val globalParameters = new DeployerParameters.Provider(() => parameters);

  def getParameters = globalParameters

  override def getParameter(name: String): Option[String] = {
    return globalParameters.getNoDefault(name)
  }

  @CliCommand(value = Array("parameters list"), help = "list currently set parameters")
  def list(@CliOption(key = Array("not-set"), mandatory = false, help = "if true prints not set parameters", specifiedDefaultValue = "false", unspecifiedDefaultValue = "false")
           env: Boolean): String = {
    DeployerParameters.getParams(getParameter(CommonParams.SHATHEL_ENV))
      .filter((x) => env || getParameter(x).isDefined)
      .map((paramName) => s"${paramName}=${getParameter(paramName).getOrElse("")}")
      .sorted
      .mkString("\n")
  }

  @CliCommand(value = Array("parameters add"), help = "Sets parameters in context")
  def add(
           @CliOption(key = Array("params"), mandatory = false, help = "map of parameters to set for other commands in format name1=value,name2=value")
           map: java.util.Map[String, String]): String = {
    setParameters(map)
    return ok();
  }

  def buildParameters(paramMap: java.util.Map[String, String]): Parameters = {
    val map = new util.HashMap[String, String]();
    if (paramMap != null) {
      map.putAll(paramMap)
    }
    parameters.hiddenBy(new MapParameters(map))
  }

  def setParameters(paramMap: java.util.Map[String, String]): Parameters = {
    parameters = buildParameters(paramMap);
    return globalParameters;
  }

}


trait ParametersKeyProvider extends MapConverterKeysProvider {
  val pattern = """.*--env-name\s+([a-zA-Z0-9]+)\s+.*""".r
  val pattern2 = """.*shathel.env=([a-zA-Z0-9]+)[,\s]+.*""".r

  override def getPossibleKeysInMap(existingUserData: String): util.List[String] = {
    existingUserData match {
      case pattern(env) => DeployerParameters.getParams(Option(env)).asJava
      case pattern2(env) => DeployerParameters.getParams(Option(env)).asJava
      case _ => DeployerParameters.getParams(getParameter(CommonParams.SHATHEL_ENV)).asJava
    }
  }

  def getParameter(name: String): Option[String]

}



