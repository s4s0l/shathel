package org.s4s0l.shathel.deployer

import org.s4s0l.shathel.commons.core.MapParameters
import org.s4s0l.shathel.commons.utils.ExtensionInterface
import org.s4s0l.shathel.commons.{DefaultExtensionContext, Shathel}
import org.springframework.shell.core.CommandMarker
import org.yaml.snakeyaml.Yaml

import scala.collection.JavaConverters._
import scala.util.Try

/**
  * @author Matcin Wielgus
  */
class ShathelCommands(parametersCommands: ParametersCommands) extends CommandMarker with ParametersKeyProvider {


  def shathel(parametersMap: java.util.Map[String, String], commandOverrides: DeployerParameters.Builder = new DeployerParameters.Builder())(work: (DeployerParameters.ShathelCommandContext) => String): String = {
    val build = commandOverrides.build()
    val buildParameters = parametersCommands.buildParameters(parametersMap)
    val allParameters = MapParameters.builder().parameters(build.asJava).build().over(buildParameters)
    val extensionContext = DefaultExtensionContext.create(allParameters, List[ExtensionInterface](new MvnDependencyDownloader(allParameters)).asJava)
    val shathel = new Shathel(allParameters, extensionContext)
    val context = new DeployerParameters.ShathelCommandContext(shathel, () => allParameters)
    val ret = work(context)
    parametersCommands.setParameters(parametersMap)
    return ret;
  }

  def builder(): DeployerParameters.Builder = new DeployerParameters.Builder()

  def response(map: Map[String, Any]): String = {
    new Yaml().dump(map.asJava)
  }

  def ok(): String = {
    new Yaml().dump(Map("status" -> "ok").asJava)
  }

  def getParameter(name: String): Option[String] = parametersCommands.getParameter(name)
}

