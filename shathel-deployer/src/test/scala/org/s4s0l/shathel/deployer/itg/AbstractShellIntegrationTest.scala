package org.s4s0l.shathel.deployer.itg

import java.io.File
import java.util

import org.apache.commons.io.FileUtils
import org.s4s0l.shathel.commons.core.CommonParams
import org.s4s0l.shathel.deployer.Main
import org.s4s0l.shathel.deployer.shell.BootShim
import org.scalatest.{BeforeAndAfterAll, FeatureSpec}
import org.slf4j.LoggerFactory
import org.springframework.boot.SpringApplication
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.shell.core.JLineShellComponent
import org.yaml.snakeyaml.Yaml

import scala.collection.JavaConverters._

/**
  * @author Marcin Wielgus
  */
class AbstractShellIntegrationTest extends FeatureSpec with BeforeAndAfterAll {
  val LOGGER = LoggerFactory.getLogger(classOf[AbstractShellIntegrationTest]);
  var shell: JLineShellComponent = null
  var ctx: ConfigurableApplicationContext = null
  var shim: BootShim = null

  def shathelShell(): JLineShellComponent = shell

  override protected def beforeAll(): Unit = {
    if (rootDir.exists()) {
      FileUtils.deleteDirectory(rootDir)
    }
    rootDir.mkdirs()
    System.setProperty(CommonParams.SHATHEL_DIR, rootDir.getAbsolutePath)
    System.setProperty(CommonParams.SHATHEL_ENV, "local")
    System.setProperty("shathel.solution.file.base.dir", new File(rootDir, "../../../shathel-commons/src/test/sampleDependencies").getAbsolutePath)
    System.setProperty("shathel.env.local.safePassword", "samplePassword")
    ctx = new SpringApplication(classOf[Main].asInstanceOf[AnyRef]).run()
    shim = new BootShim(Array(), ctx)
    shell = ctx.getBean("shell", classOf[JLineShellComponent])
  }

  def rootDir = {
    val file = new File(s"./build/${getClass.getSimpleName}")
    file.mkdirs()
    file.toPath.toRealPath().normalize().toFile
  }


  override protected def afterAll(): Unit = {
    ctx.close()
    ctx = null
    shim = null
    shell = null
  }

  def assertCmdSuccess(command: String): String = {
    LOGGER.info("RUNNING:" + command)
    val command1 = shell.executeCommand(command)
    assert(command1.isSuccess)
    val s = command1.getResult.asInstanceOf[String]
    if (s == null) {
      return ""
    } else {
      s.trim
    }
  }

  def assertCmdResult(command: String, expected: => String): Unit = {
    LOGGER.info("RUNNING:" + command)
    var res = shell.executeCommand(command)
    assert(res.isSuccess)
    assert(res.getResult.asInstanceOf[String].trim == expected.stripMargin.trim)
  }

  def assertCmd(command: String)(assertion: String => Unit): Unit = {
    LOGGER.info("RUNNING:" + command)
    var res = shell.executeCommand(command)
    assert(res.isSuccess)
    assertion(res.getResult.asInstanceOf[String].trim)
  }

  def assertCmdResult(command: String, expected: AnyRef): Unit = {
    LOGGER.info("RUNNING:" + command)
    var res = shell.executeCommand(command)
    assert(res.isSuccess)
    var yml = new Yaml().load(res.getResult.asInstanceOf[String])

    assert(yml == expected)
  }

  def assertCmdYmlResult(command: String, expected: String): Unit = {

    val map: AnyRef = new Yaml().load(expected.stripMargin)
    assertCmdResult(command, map)
  }


  def assertFileExists(path: String): Unit = {
    assert(new File(rootDir, path).exists())
  }

  def assertFileNotExists(path: String): Unit = {
    assert(!new File(rootDir, path).exists())
  }
}
