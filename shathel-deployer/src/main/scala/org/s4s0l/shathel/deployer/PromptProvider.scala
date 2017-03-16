package org.s4s0l.shathel.deployer

import org.springframework.shell.plugin.support.DefaultPromptProvider

import scala.Option

/**
  * @author Marcin Wielgus
  */
class PromptProvider(params: ParametersCommands) extends DefaultPromptProvider {


  override def getPrompt = s"$$shathel ${params.getParameters.environment()}>"
}
