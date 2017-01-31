package org.s4s0l.shathel.deployer.shell.customization

/**
  * @author Matcin Wielgus
  */
trait MapConverterKeysProvider {
  def getPossibleKeysInMap(existingUserData: String): java.util.List[String]
}
