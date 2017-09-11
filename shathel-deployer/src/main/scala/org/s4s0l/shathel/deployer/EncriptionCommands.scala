package org.s4s0l.shathel.deployer

import java.io.{Console, File}

import jline.console.ConsoleReader
import org.codehaus.groovy.runtime.ResourceGroovyMethods
import org.s4s0l.shathel.commons.core.storage.Storage
import org.s4s0l.shathel.deployer.DeployerParameters.ShathelCommandContext
import org.springframework.shell.core.annotation.{CliCommand, CliOption}


/**
  * @author Marcin Wielgus
  */
class EncriptionCommands(parametersCommands: ParametersCommands, ec: EnvironmentCommands) extends ShathelCommands(parametersCommands) {


  @CliCommand(value = Array("encrypt"), help = "Encrypts given string with current env safe password, first call environment use")
  def encrypt(
               @CliOption(key = Array("file"), mandatory = false, help = "directory with shathel-solution.yml file")
               file: File,
               @CliOption(key = Array("params"), mandatory = false, help = "map of parameters to set for this command only, format name1=value,name2=value",
                 specifiedDefaultValue = "", unspecifiedDefaultValue = "")
               map: java.util.Map[String, String]): String = {
    shathel(map, builder())(
      context => {
        val (_, _, environment) = ec.getEnvironment(context)
        if (file == null) {
          val cr = new ConsoleReader()
          val pwd = cr.readLine('*')
          environment.getEnvironmentContext.getSafeStorage.crypt(pwd.toCharArray)
        } else {
          val pwd = ResourceGroovyMethods.getText(file)
          environment.getEnvironmentContext.getSafeStorage.crypt(pwd.toCharArray)
        }
      })

  }

  @CliCommand(value = Array("decrypt"), help = "Encrypts given string with current env safe password, first call environment use")
  def decrypt(
               @CliOption(key = Array("value", ""), mandatory = true, help = "The value to decrypt")
               value: String,
               @CliOption(key = Array("params"), mandatory = false, help = "map of parameters to set for this command only, format name1=value,name2=value",
                 specifiedDefaultValue = "", unspecifiedDefaultValue = "")
               map: java.util.Map[String, String]): String = {
    shathel(map, builder())(
      context => {
        val (_, _, environment) = ec.getEnvironment(context)
        environment.getEnvironmentContext.getSafeStorage.decrypt(value)
      })

  }

  def getStorage(ctxt: ShathelCommandContext): Storage = {
    val file = ctxt.storageFile()
    ctxt.shathel.initStorage(file, false)
  }

}