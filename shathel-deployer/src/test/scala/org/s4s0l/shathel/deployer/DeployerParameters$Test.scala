package org.s4s0l.shathel.deployer

import org.junit.runner.RunWith
import org.scalatest.{FeatureSpec, GivenWhenThen}
import org.scalatest.junit.JUnitRunner

/**
  * @author Marcin Wielgus
  */
@RunWith(classOf[JUnitRunner])
class DeployerParameters$Test extends FeatureSpec with GivenWhenThen {
  feature("Methods tests") {
    scenario("No env") {
      When("get list of allowed params")
      val params = DeployerParameters.getParams(Option.empty)
      Then("Params contain only non env params")
      assert(params.size == 12)
    }

    scenario("With env") {
      When("get list of allowed params")
      val params = DeployerParameters.getParams(Option("itg"))
      Then("Params contain only non env params")
      assert(params.size == DeployerParameters.VALID_PARAMS.size)
      assert(params.filter((x)=>x.contains(".itg.")).size == 15)
      assert(params.filter((x)=>x.contains("{env}")).isEmpty)
    }
  }
}
