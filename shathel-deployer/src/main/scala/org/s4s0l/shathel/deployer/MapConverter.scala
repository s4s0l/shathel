package org.s4s0l.shathel.deployer

import java.util

import org.springframework.shell.core.{Completion, Converter, MethodTarget}

import scala.collection.JavaConverters._

/**
  * @author Matcin Wielgus
  */
class MapConverter extends Converter[java.util.Map[String, String]] {
  override def convertFromText(value: String, targetType: Class[_], optionContext: String): java.util.Map[String, String] = {
    return Map("asdasd" -> value).asJava
  }

  override def getAllPossibleValues(completions: util.List[Completion],
                                    targetType: Class[_], existingData: String,
                                    optionContext: String, target: MethodTarget): Boolean = {
    if (existingData.equals("dupa=") || existingData.equals("dupa= ")) {
      completions.add(new Completion("dupa=axxx"))
      completions.add(new Completion("dupa=bxxx"))
      return true
    } else {
      completions.add(new Completion("dupa=","dupa=","adadsasd", 0))
      return false
    }
  }

  override def supports(t: Class[_], optionContext: String): Boolean = {
    return t.equals(classOf[java.util.Map[String, String]])
  }
}
