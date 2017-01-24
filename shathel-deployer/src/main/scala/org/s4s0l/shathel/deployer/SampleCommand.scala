package org.s4s0l.shathel.deployer

import org.springframework.shell.core.CommandMarker
import org.springframework.shell.core.annotation.{CliCommand, CliOption}



/**
  * @author Matcin Wielgus
  */
class SampleCommand extends CommandMarker {
  @CliCommand(value = Array("hw"), help = "Print a simple hello world message")
  def simple(
              @CliOption(key = Array("message"), mandatory = true, help = "The hello world message")
              message: String,
              @CliOption(key = Array("location"), mandatory = false, help = "Where you are saying hello", specifiedDefaultValue = "At work")
              location: String,
              @CliOption(key = Array("map"), mandatory = false)
              map: java.util.Map[String, String],
              @CliOption(key = Array("f"), mandatory = false)
              file: java.io.File
            )
  : String = {
    return "Message = [" + message + "] Location = [" + location + "]" + map
  }
}


