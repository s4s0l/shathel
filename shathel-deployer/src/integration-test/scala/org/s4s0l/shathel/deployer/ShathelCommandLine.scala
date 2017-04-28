package org.s4s0l.shathel.deployer

import java.io.{File, PrintWriter}
import java.util.UUID

import org.s4s0l.shathel.commons.utils.Utils

import scala.sys.process._

/**
  * @author Marcin Wielgus
  */
trait ShathelCommandLine {


  def version = "0.0.4"

  def shathel(): String = {
    val version = Utils.getShathelVersion
    val x = s"../build/localrepo/org/s4s0l/shathel/shathel-deployer/${version}/shathel-deployer-${version}.jar"
    new File(x).setExecutable(true)
    x
  }

  def script(scriptTemplate: String): String = {
    val prefix = s"parameters add --params shathel.dir=%ROOT%/shathel-deployer/build/Test${getClass.getSimpleName}"
    s"""${prefix}
       |${scriptTemplate.stripMargin}
       """.stripMargin
      .replace("%ROOT%", new File("./..").getAbsolutePath)
      .replace("%version%", version)
  }

  def fileScript(scriptTemplate: String) = {
    new File(s"build/Test${getClass.getSimpleName}").mkdirs()
    val scripts = script(scriptTemplate)
    val out = new File(s"build/Test${getClass.getSimpleName}/${UUID.randomUUID().toString}.sht");
    new PrintWriter(out) {
      write(scripts);
      close
    }
    out.getAbsolutePath
  }

  def runScript(scriptTemplate: String): Unit = {
    assert(0 == ((s"${shathel()} --cmdfile ${fileScript(scriptTemplate)}".toString) !))
  }
}
