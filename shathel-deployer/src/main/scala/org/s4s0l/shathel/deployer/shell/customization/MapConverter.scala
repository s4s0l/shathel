package org.s4s0l.shathel.deployer.shell.customization

import java.util

import org.springframework.shell.core.{Completion, Converter, MethodTarget}

import scala.collection.JavaConverters._


/**
  * @author Marcin Wielgus
  */
class MapConverter extends Converter[java.util.Map[String, String]] {


  override def convertFromText(value: String, targetType: Class[_], optionContext: String): java.util.Map[String, String] = {
    toMap(value).asJava
  }

  private def toMap(value: String): Map[String, String] = {
    def arrayToMap = collection.breakOut[Array[String], (String, String), Map[String, String]]

    value match {
      case null => Map[String, String]()
      case value => value.split(",").map(x => {
        x.split("=") match {
          case Array("") => (null, null)
          case Array("", b@_*) => (null, b.mkString("="))
          case Array(a, "") => (a, null)
          case Array(a, b) => (a.trim, b.trim)
          case Array(a) => (a.trim, null)
          case Array(a, x@_*) => (a.trim, x.mkString("="))
        }
      })(arrayToMap).filter(_._1 != null)
    }
  }

  private def optionContextAsList(optionContext: String, target: MethodTarget,
                                  existingData: String): Seq[String] = {

    val filter = if (target == null) Option.empty[MapConverterKeysProvider] else if (target.getTarget.isInstanceOf[MapConverterKeysProvider]) Option(target.getTarget.asInstanceOf[MapConverterKeysProvider]) else Option.empty[MapConverterKeysProvider]
    if (filter.isDefined) {
      val strings: util.List[String] = filter.get.getPossibleKeysInMap(existingData)
      return strings.asScala
    } else {
      if (optionContext == null) {
        return Seq()
      }
      val o = if (optionContext.contains(Converter.TAB_COMPLETION_COUNT_PREFIX)) optionContext.substring(optionContext.indexOf(" ") + 1, optionContext.length) else optionContext
      return o.split(",").map(_.trim).filter(!_.equals(""))
    }
  }


  override def getAllPossibleValues(completions: util.List[Completion],
                                    targetType: Class[_], existingData: String,
                                    optionContext: String, target: MethodTarget): Boolean = {
    val cmp = optionContextAsList(optionContext, target, existingData)
    val input = toMap(existingData)
    val lastInputPart = existingData match {
      case null => ""
      case a if existingData.lastIndexOf(',') == -1 => a.trim
      case a if existingData.lastIndexOf(',') == existingData.length - 1 => ","
      case a => a.substring(existingData.lastIndexOf(',') + 1).trim
    }
    (cmp.filter(!input.contains(_)), lastInputPart) match {
      case (Seq(), _) => true
      case (options, "") => {
        completions.addAll(options.map(_ + "=").map(new Completion(_)).asJava)
        false
      }
      case (options, a) if a.contains("=") => {
        completions.addAll(options.map(existingData.trim + "," + _ + "=").map(new Completion(_)).asJava)
        false
      }
      case (options, a) if a.endsWith(",") => {
        completions.addAll(options.map(existingData.trim + _ + "=").map(new Completion(_)).asJava)
        false
      }
      case (options, a) if cmp.contains(a) => {
        completions.add(new Completion(existingData.trim + "="))
        false
      }
      case (options, a) if !options.filter(_.startsWith(a)).isEmpty => {
        completions.addAll(options.filter(_.startsWith(a)).map(existingData.trim + _.substring(a.length) + "=").map(new Completion(_)).asJava)
        false
      }
      case (_, _) => true
    }

  }

  override def supports(t: Class[_], optionContext: String): Boolean = {
    return t.equals(classOf[java.util.Map[String, String]])
  }
}
