package org.s4s0l.shathel.deployer

import java.util.Collections

import org.scalatest.FeatureSpec

import scala.collection.JavaConverters._

/**
  * @author Matcin Wielgus
  */
class ParametersCommandsTest extends FeatureSpec {
  feature("Methods tests") {

    scenario("list") {
      val cmds = new ParametersCommands
      val list = cmds.list(true)
      assert(list.contains("shathel.env="))
      assert(!list.contains("shathel.storage.work"))
      System.setProperty("shathel.env", "itg")
      val list2 = cmds.list(true)
      assert(list2.contains("shathel.env=itg"))
      assert(list2.contains("shathel.storage.work.itg.dir="))
      val list3 = cmds.list(false)
      assert(list3.contains("shathel.env=itg"))
      assert(!list3.contains("shathel.storage.work.itg.dir="))
    }

    scenario("add") {
      val cmds = new ParametersCommands
      cmds.add(Collections.singletonMap("shathel.env", "dev"))
      assert(cmds.list(false).contains("shathel.env="))

      System.setProperty("shathel.env", "dev")
      assert(cmds.list(false).contains("shathel.env=itg"))

      cmds.add(Map("dupa" -> "eeee").asJava)
      assert(cmds.list(false).contains("dupa=eeee"))

    }


  }
}
