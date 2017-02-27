package org.s4s0l.shathel.deployer

import java.io.File

import org.s4s0l.shathel.commons.core.storage.Storage
import org.s4s0l.shathel.deployer.DeployerParameters.ShathelCommandContext
import org.springframework.shell.core.annotation.{CliCommand, CliOption}


/**
  * @author Matcin Wielgus
  */
class StorageCommands(parametersCommands: ParametersCommands) extends ShathelCommands(parametersCommands) {


  @CliCommand(value = Array("storage verify"), help = "Checks current storage status")
  def status(
              @CliOption(key = Array("file"), mandatory = false, help = "directory with shathel-solution.yml file")
              file: File,
              @CliOption(key = Array("params"), mandatory = false, help = "map of parameters to set for this command only, format name1=value,name2=value",
                specifiedDefaultValue = "", unspecifiedDefaultValue = "")
              map: java.util.Map[String, String]): String = {
    shathel(map, builder()
      .storageFile(file)
    )(context => {
      getStorage(context).verify()
      "OK";
    })

  }

  def getStorage(ctxt: ShathelCommandContext): Storage = {
    val file = ctxt.storageFile()
    if (!file.exists() && ctxt.storageInit()) {
      ctxt.shathel.initStorage(file, true)
    } else {
      ctxt.shathel.getStorage(file)
    }
  }

}