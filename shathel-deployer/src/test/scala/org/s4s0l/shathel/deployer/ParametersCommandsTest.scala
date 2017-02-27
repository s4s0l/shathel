package org.s4s0l.shathel.deployer

import java.util.Collections

import org.junit.runner.RunWith
import org.scalatest.FeatureSpec
import org.scalatest.junit.JUnitRunner

import scala.collection.JavaConverters._

/**
  * @author Matcin Wielgus
  */
@RunWith(classOf[JUnitRunner])
class ParametersCommandsTest extends FeatureSpec {
  feature("Methods tests") {

    scenario("list") {
      val cmds = new ParametersCommands
      val list = cmds.list(true)
      assert(list.contains("shathel.env="))
      System.setProperty("shathel.env", "itg")
      val list2 = cmds.list(true)
      assert(list2.contains("shathel.env=itg"))
      assert(list2.contains("shathel.env.itg.enrichedDir="))
      val list3 = cmds.list(false)
      assert(list3.contains("shathel.env=itg"))
      assert(!list3.contains("shathel.env.itg.enrichedDir="))
    }

    scenario("add") {
      val cmds = new ParametersCommands
      cmds.add(Collections.singletonMap("shathel.env", "dev"))
      assert(cmds.list(false).contains("shathel.env="))

      System.setProperty("shathel.env", "dev")
      assert(cmds.list(false).contains("shathel.env=dev"))

      cmds.add(Map("dupa" -> "eeee").asJava)
      assert(!cmds.list(false).contains("dupa=eeee"))

      cmds.add(Map("dupa" -> "eeee").asJava)
      assert(!cmds.list(true).contains("dupa=eeee"))
    }


  }
}
