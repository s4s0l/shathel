package org.s4s0l.shathel.deployer

import java.io.{File, PrintWriter}
import java.util.UUID

import org.junit.runner.RunWith
import org.scalatest.FeatureSpec
import org.scalatest.junit.JUnitRunner

import sys.process._

/**
  * @author Marcin Wielgus
  */
@RunWith(classOf[JUnitRunner])
class DeployerTest extends FeatureSpec with ShathelCommandLine {
  feature("Starting stopping stacks") {
    scenario("in local environment") {
      runScript(
        """environment use local
          |stack start --name git@github.com/s4s0l/shathel-stacks:core:%version%
          |stack start --name git@github.com/s4s0l/shathel-stacks:portainer:%version% --inspect 0""")
      runScript(
        """
          |stack stop --name git@github.com/s4s0l/shathel-stacks:portainer:%version% --with-dependencies --with-optional 1""")
    }
  }
}
