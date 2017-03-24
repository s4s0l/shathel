package org.s4s0l.shathel.deployer

import java.util.Collections

import org.junit.runner.RunWith
import org.s4s0l.shathel.commons.core.CommonParams
import org.scalatest.FeatureSpec
import org.scalatest.junit.JUnitRunner

import scala.collection.JavaConverters._

/**
  * @author Marcin Wielgus
  */
@RunWith(classOf[JUnitRunner])
class ParametersCommandsTest extends FeatureSpec {
  feature("Methods tests") {

    scenario("list") {
      val cmds = new ParametersCommands
      val list = cmds.list(true)
      assert(list.contains(s"${CommonParams.SHATHEL_ENV}="))
      System.setProperty(CommonParams.SHATHEL_ENV, "itg")
      val list2 = cmds.list(true)
      assert(list2.contains(s"${CommonParams.SHATHEL_ENV}=itg"))
      assert(list2.contains("shathel.env.itg.enrichedDir="))
      val list3 = cmds.list(false)
      assert(list3.contains(s"${CommonParams.SHATHEL_ENV}=itg"))
      assert(!list3.contains("shathel.env.itg.enrichedDir="))
    }

    scenario("add") {
      val cmds = new ParametersCommands
      cmds.add(Collections.singletonMap(CommonParams.SHATHEL_ENV, "dev"))
      assert(cmds.list(false).contains(s"${CommonParams.SHATHEL_ENV}="))

      System.setProperty(CommonParams.SHATHEL_ENV, "dev")
      assert(cmds.list(false).contains(s"${CommonParams.SHATHEL_ENV}=dev"))

      cmds.add(Map("dupa" -> "eeee").asJava)
      assert(!cmds.list(false).contains("dupa=eeee"))

      cmds.add(Map("dupa" -> "eeee").asJava)
      assert(!cmds.list(true).contains("dupa=eeee"))
    }


  }
}
