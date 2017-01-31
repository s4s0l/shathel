package org.s4s0l.shathel.deployer.shell.customization

import java.util

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{FeatureSpec, GivenWhenThen}
import org.springframework.shell.core.{Completion, MethodTarget}
import org.springframework.shell.core.annotation.{CliCommand, CliOption}

import scala.collection.JavaConverters._
/**
  * @author Matcin Wielgus
  */
@RunWith(classOf[JUnitRunner])
class MapConverterTest extends FeatureSpec with GivenWhenThen {

  feature("Methods tests") {
    val x = new MapConverter
    scenario("convertFromText") {

      When("parsing null value")
      val ret = x.convertFromText(null, classOf[java.util.Map[String,String]], null)
      Then("resulting map is empty")
      assert(ret.equals(Map().asJava))

      When("parsing empty value")
      val ret1 = x.convertFromText("", classOf[java.util.Map[String,String]], null)
      Then("resulting map is empty")
      assert(ret1.equals(Map().asJava))

      When("parsing elemnt without value")
      val ret2 = x.convertFromText("a", classOf[java.util.Map[String,String]], null)
      Then("resulting in single entry")
      assert(ret2.equals(Map("a"->null).asJava))

      When("parsing elements with spaces")
      val ret3 = x.convertFromText("a=aval , b = bvalue with space  ", classOf[java.util.Map[String,String]], null)
      Then("resulting in trimmed entries")
      assert(ret3.equals(Map("a"->"aval", "b"->"bvalue with space").asJava))


      When("parsing elements with no name")
      val ret4 = x.convertFromText("=a,=a=b,,,,,c=b", classOf[java.util.Map[String,String]], null)
      Then("resulting in unnamed skipped")
      assert(ret4.equals(Map("c"->"b").asJava))


      When("parsing elements with multiple = in value")
      val ret5 = x.convertFromText("a=a,b=b=b,c=c=c=c", classOf[java.util.Map[String,String]], null)
      Then("resulting in mapped value")
      assert(ret5.equals(Map("a"->"a", "b"->"b=b", "c"->"c=c=c").asJava))
    }

    scenario("supports") {
      When("java.util.Map")
      val supports = x.supports(classOf[util.Map[String,String]], null)
      Then("True")
      assert(supports)

      When("java.util.List")
      val supports2 = x.supports(classOf[util.List[String]], null)
      Then("false")
      assert(!supports2)
    }

    scenario("getAllPossibleValues") {
      {
        When("no context")
        val list = new util.ArrayList[Completion]()
        val complete = x.getAllPossibleValues(list, classOf[util.Map[String,String]], null, null, null)
        Then("No completions provided")
        assert(list.equals(List().asJava))
        assert(complete)
      }

      {
        When("withContext no input")
        val list = new util.ArrayList[Completion]()
        val complete = x.getAllPossibleValues(list, classOf[util.Map[String,String]], null, "a,b", null)
        Then("True")
        assert(list.equals(List(new Completion("a="),new Completion("b=")).asJava))
        assert(!complete)
      }

      {
        When("withContext no input")
        val list = new util.ArrayList[Completion]()
        val complete = x.getAllPossibleValues(list, classOf[util.Map[String,String]], "", "a,b", null)
        Then("True")
        assert(list.equals(List(new Completion("a="),new Completion("b=")).asJava))
        assert(!complete)
      }

      {
        When("withContext partial unknown input")
        val list = new util.ArrayList[Completion]()
        val complete = x.getAllPossibleValues(list, classOf[util.Map[String,String]], "c=", "a,b", null)
        Then("True")
        assert(list.equals(List(new Completion("c=,a="),new Completion("c=,b=")).asJava))
        assert(!complete)
      }

      {
        When("withContext full unknown input")
        val list = new util.ArrayList[Completion]()
        val complete = x.getAllPossibleValues(list, classOf[util.Map[String,String]], "c=aaa", "a,b", null)
        Then("True")
        assert(list.equals(List(new Completion("c=aaa,a="),new Completion("c=aaa,b=")).asJava))
        assert(!complete)
      }

      {
        When("withContext full unknown input with comma")
        val list = new util.ArrayList[Completion]()
        val complete = x.getAllPossibleValues(list, classOf[util.Map[String,String]], "c=aaa,", "a,b", null)
        Then("True")
        assert(list.equals(List(new Completion("c=aaa,a="),new Completion("c=aaa,b=")).asJava))
        assert(!complete)
      }

      {
        When("withContext full known input with comma")
        val list = new util.ArrayList[Completion]()
        val complete = x.getAllPossibleValues(list, classOf[util.Map[String,String]], "a=aaa,", "a,b", null)
        Then("True")
        assert(list.equals(List(new Completion("a=aaa,b=")).asJava))
        assert(!complete)
      }

      {
        When("withContext full known input with no comma")
        val list = new util.ArrayList[Completion]()
        val complete = x.getAllPossibleValues(list, classOf[util.Map[String,String]], "a=aaa", "a,b", null)
        Then("True")
        assert(list.equals(List(new Completion("a=aaa,b=")).asJava))
        assert(!complete)
      }

      {
        When("withContext partial known input with some filled")
        val list = new util.ArrayList[Completion]()
        val complete = x.getAllPossibleValues(list, classOf[util.Map[String,String]], "c=c,a", "aaaa,aAAA,bb", null)
        Then("True")
        assert(list.equals(List(new Completion("c=c,aaaa="),new Completion("c=c,aAAA=")).asJava))
        assert(!complete)
      }

      {
        When("withContext partial known input with some filled")
        val list = new util.ArrayList[Completion]()
        val complete = x.getAllPossibleValues(list, classOf[util.Map[String,String]], "aaaa", "aaaa,aAAA,bb", null)
        Then("True")
        assert(list.equals(List(new Completion("aaaa=")).asJava))
        assert(!complete)
      }

      {
        When("withContext partial known input")
        val list = new util.ArrayList[Completion]()
        val complete = x.getAllPossibleValues(list, classOf[util.Map[String,String]], "a", "aaaa,aAAA,bb", null)
        Then("True")
        assert(list.equals(List(new Completion("aaaa="),new Completion("aAAA=")).asJava))
        assert(!complete)
      }

      {
        When("withContext partial known input")
        val list = new util.ArrayList[Completion]()
        val complete = x.getAllPossibleValues(list, classOf[util.Map[String,String]], "a=asd,b=asd", "a,b", null)
        Then("True")
        assert(list.equals(List().asJava))
        assert(complete)
      }

    }
    scenario("convertFromText with completion list provided by method") {
      {
        When("withContext partial known input")
        val list = new util.ArrayList[Completion]()
        val meth: MethodTarget = new MethodTarget(classOf[X].getMethod("simple",classOf[java.util.Map[String, String]]), new X)
        val complete = x.getAllPossibleValues(list, classOf[util.Map[String,String]], "", "aaaaa,bbbb", meth)
        Then("True")
        assert(list.equals(List(new Completion("xxx="),new Completion("yyy=")).asJava))
        assert(!complete)
      }
    }
  }
}


class X extends MapConverterKeysProvider{
  @CliCommand(value = Array("hw"), help = "Print a simple hello world message")
  def simple(
              @CliOption(key = Array("map"), mandatory = false, optionContext = "aaa,bbb,ccc,aab")
              map: java.util.Map[String, String]
            )
  : String = {
    return ???
  }

  override def getPossibleKeysInMap(existingUserData: String): util.List[String] = List("xxx","yyy").asJava


}