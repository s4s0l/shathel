package org.s4s0l.shathel.deployer

import org.s4s0l.shathel.commons.core.MapParameters
import org.s4s0l.shathel.commons.{DefaultExtensionContext, Shathel}
import org.springframework.shell.core.CommandMarker

import scala.collection.JavaConverters._

/**
  * @author Marcin Wielgus
  */
class ShathelCommands(parametersCommands: ParametersCommands) extends CommandMarker with ParametersKeyProvider with OutputFormatter {


  def shathel(parametersMap: java.util.Map[String, String],
              commandOverrides: DeployerParameters.Builder = new DeployerParameters.Builder())
             (work: (DeployerParameters.ShathelCommandContext) => String)
  : String = {
    val commandOverridesParams = commandOverrides.build()
    val buildParameters = parametersCommands.buildParameters(parametersMap)
    val allParameters = buildParameters.hiddenBy(new MapParameters(commandOverridesParams.asJava))
    val extensionContext = DefaultExtensionContext.create()
    val shathel = new Shathel(allParameters, extensionContext)
    val context = DeployerParameters.ShathelCommandContext(shathel, () => allParameters)
    val ret = work(context)
    parametersCommands.setParameters(parametersMap)
    ret
  }


  def builder(): DeployerParameters.Builder = new DeployerParameters.Builder()


  def getParameter(name: String): Option[String] = parametersCommands.getParameter(name)
}

