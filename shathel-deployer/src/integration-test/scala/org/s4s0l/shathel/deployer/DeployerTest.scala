package org.s4s0l.shathel.deployer

import java.io.{File, PrintWriter}

import org.junit.runner.RunWith
import org.scalatest.FeatureSpec
import org.scalatest.junit.JUnitRunner
import sys.process._

/**
  * @author Marcin Wielgus
  */
@RunWith(classOf[JUnitRunner])
class DeployerTest extends FeatureSpec {

  def getPath(): String = {
    val x = "../build/localrepo/org/s4s0l/shathel/shathel-deployer/DEVELOPER-SNAPSHOT/shathel-deployer-DEVELOPER-SNAPSHOT.jar"
    new File(x).setExecutable(true)
    x
  }

  def destroyCommand = {
    "environment destroy --params " +
      "shathel.env=dev,shathel.mvn.localRepo=$ROOT/../build/localrepo,shathel.deployer.dir=$ROOT/build/TempSolution"
        .replace("$ROOT", new File(".").getAbsolutePath)
  }

  def prepareScript(scriptTemplate: String) = {
    new File(s"build/${getClass.getSimpleName}").mkdirs()
    val script = scala.io.Source.fromURL(getClass.getResource(s"/${scriptTemplate}"))
      .mkString
      .replace("$ROOT", new File("./..").getAbsolutePath)
    val out = new File(s"build/${getClass.getSimpleName}/${scriptTemplate}");
    new PrintWriter(out) {
      write(script);
      close
    }
    out.getAbsolutePath
  }

  feature("Starting stopping stacks") {
    scenario("in composed environment") {
      assert(0 == ((s"${getPath()} --cmdfile ${prepareScript("script-compose")}".toString) !))
      assert(0 == ((s"${getPath()} --cmdfile ${prepareScript("script-compose-stop")}") !))
    }

    scenario("in dind environment") {
      def cmd1 = s"${getPath()} --cmdfile ${prepareScript("script-dind")}";
      assert(0 == ((cmd1) !))
      assert(0 == ((s"${getPath()} --cmdfile ${prepareScript("script-dind-stop")}".toString) !))

      def cmd3 = s"${getPath()} ${destroyCommand}"

      assert(0 == ((cmd3) !))
    }
  }
}
