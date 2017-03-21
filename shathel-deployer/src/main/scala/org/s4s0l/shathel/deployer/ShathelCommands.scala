package org.s4s0l.shathel.deployer

import org.s4s0l.shathel.commons.core.dependencies.FileDependencyDownloader
import org.s4s0l.shathel.commons.core.{MapParameters, Parameters}
import org.s4s0l.shathel.commons.git.GitDependencyDownloader
import org.s4s0l.shathel.commons.utils.ExtensionInterface
import org.s4s0l.shathel.commons.{DefaultExtensionContext, Shathel}
import org.springframework.shell.core.CommandMarker
import org.yaml.snakeyaml.{DumperOptions, Yaml}

import scala.collection.JavaConverters._
import scala.util.Try

/**
  * @author Marcin Wielgus
  */
class ShathelCommands(parametersCommands: ParametersCommands) extends CommandMarker with ParametersKeyProvider {


  def shathel(parametersMap: java.util.Map[String, String], commandOverrides: DeployerParameters.Builder = new DeployerParameters.Builder())(work: (DeployerParameters.ShathelCommandContext) => String): String = {
    val commandOverridesParams = commandOverrides.build()
    val buildParameters = parametersCommands.buildParameters(parametersMap)
    val allParameters = buildParameters.hiddenBy(MapParameters.builder().parameters(commandOverridesParams.asJava).build())
    val extensionContext = DefaultExtensionContext.create(allParameters)
    val shathel = new Shathel(allParameters, extensionContext)
    val context = new DeployerParameters.ShathelCommandContext(shathel, () => allParameters)
    val ret = work(context)
    parametersCommands.setParameters(parametersMap)
    return ret;
  }


  def builder(): DeployerParameters.Builder = new DeployerParameters.Builder()


  def yaml: Yaml = {
    val options = new DumperOptions()
    options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK)
    new Yaml(options)
  }

  def response(map: Map[String, Any]): String = {
    yaml.dump(map.asJava)
  }

  def response(map: List[String]): String = {
    yaml.dump(map.asJava)
  }

  def response(map: java.util.List[String]): String = {
    yaml.dump(map)
  }

  def ok(): String = {
    yaml.dump(Map("status" -> "ok").asJava)
  }

  def getParameter(name: String): Option[String] = parametersCommands.getParameter(name)
}

