package org.s4s0l.shathel.deployer

import org.springframework.shell.plugin.support.DefaultPromptProvider

import scala.Option

/**
  * @author Matcin Wielgus
  */
class PromptProvider(params: ParametersCommands) extends DefaultPromptProvider {


  override def getPrompt = {
    params.getParameters.getNoDefault("shathel.env") match {
      case Some(some) => s"$$shathel ${some}>"
      case None => "$shathel>"
    }

  }
}
