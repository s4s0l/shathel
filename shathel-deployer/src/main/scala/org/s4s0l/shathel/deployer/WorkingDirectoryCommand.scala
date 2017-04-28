package org.s4s0l.shathel.deployer

import java.io.File

import org.springframework.shell.core.CommandMarker
import org.springframework.shell.core.annotation.{CliCommand, CliOption}

/**
  * @author Marcin Wielgus
  */
class WorkingDirectoryCommand extends CommandMarker {

  @CliCommand(value = Array("cd"), help = "Changes working directory for ~ commands.")
  def cd(@CliOption(key = Array("", " dir"), mandatory = false, help = "working dir")
         path: File): String = {
    if (path.isAbsolute) {
      System.setProperty("user.dir", path.getAbsolutePath);
    } else {
      val path1 = new File(".", path.getPath).getAbsolutePath
      System.setProperty("user.dir", path1);
    }
    return new File(".").getAbsolutePath
  }
}
