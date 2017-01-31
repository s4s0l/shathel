package org.s4s0l.shathel.deployer

import org.s4s0l.shathel.commons.{DefaultExtensionContext, Shathel}
import org.springframework.shell.core.CommandMarker
import org.yaml.snakeyaml.Yaml
import scala.collection.JavaConverters._
import scala.util.Try

/**
  * @author Matcin Wielgus
  */
class ShathelCommands(parametersCommands: ParametersCommands) extends CommandMarker with ParametersKeyProvider {


  def shathel(map: java.util.Map[String, String], extra: DeployerParameters.Builder = new DeployerParameters.Builder())(work: (DeployerParameters.ShathelCommandContext) => String): String = {
    val build = extra.build()
    val buildParameters = parametersCommands.buildParameters(map, build)
    val extensionContext = DefaultExtensionContext.getExtensionBuilder(buildParameters)
    extensionContext.extension(new MvnDependencyDownloader(buildParameters))
    val shathel = new Shathel(buildParameters, extensionContext.build())
    val context = new DeployerParameters.ShathelCommandContext(shathel, () => buildParameters)
    val ret = work(context)
    parametersCommands.setParameters(map, build)
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

