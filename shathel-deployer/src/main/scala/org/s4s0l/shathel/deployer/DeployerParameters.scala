package org.s4s0l.shathel.deployer

import java.io.File
import java.util
import java.util.Optional

import org.s4s0l.shathel.commons.Shathel
import org.s4s0l.shathel.commons.core.Parameters

import scala.util.Try

/**
  * @author Matcin Wielgus
  */
object DeployerParameters {
  val VALID_PARAMS = List(
    "shathel.env",
    "shathel.mvn.repoId",
    "shathel.mvn.repoUrl",
    "shathel.mvn.settings",
    "shathel.mvn.securitySetting",
    "shathel.mvn.localRepo",
    "shathel.storage.file",
    "shathel.storage.init",
    "shathel.solution.name",
    //    "shathel.safe.{env}.password",
    "shathel.env.{env}.net",
    "shathel.env.{env}.managers",
    "shathel.env.{env}.workers",
    "shathel.storage.tmp.dependencies.dir",
    "shathel.storage.tmp.{env}.dir",
    "shathel.storage.work.{env}.dir",
    "shathel.storage.data.safe.{env}.dir"
  )


  def getParams(env: Option[String]): List[String] = {
    return VALID_PARAMS
      .filter((x: String) =>
        env
          .map((a: String) => true)
          .orElse(Option.apply(!x.contains("{env}")))
          .get)
      .map((x: String) =>
        env
          .map((i) => x.replace("{env}", i))
          .orElse(Option(x))
          .get)
  }

  val defaults: Map[String, String] = Map(
    "shathel.storage.file" -> ".",
    "shathel.storage.init" -> "true"
  )

  class Builder(values: Map[String, String] = Map()) {

    def apply(param: (String, Option[String])) = new Builder(values + (param._1 -> param._2.getOrElse(values.getOrElse(param._1, null))));

    def storageFile(f: File): Builder = this ("shathel.storage.file" -> Option(f).map(_.getAbsolutePath))

    def storageInit(f: java.lang.Boolean): Builder = this ("shathel.storage.init" -> Option(f).map(_.toString))

    def environment(f: String): Builder = this ("shathel.env" -> Option(f).map(_.toString))

    def build(): Map[String, String] = values
  }

  class Provider(provider: () => Parameters) extends Parameters {


    def storageFile(): File = new File(mandatoryParam("shathel.storage.file"))

    def storageInit(): Boolean = mandatoryBoolean("shathel.storage.init")

    def environment(): String = mandatoryParam("shathel.env")


    override def getParameter(name: String): Optional[String] = provider().getParameter(name);

    def getNoDefault(name: String): Option[String] =
      provider().getParameter(name).map[Option[String]]((t: String) => Option(t)).orElseGet(() => None)

    def getWithDefault(name: String): Option[String] = {
      provider().getParameter(name).map[Option[String]]((t: String) => Option(t)).orElseGet(() => {
        defaults.get(name)
      })
    }

    def mandatoryParam(name: String): String =
      getWithDefault(name).getOrElse(missing(name))

    def mandatoryBoolean(name: String): Boolean = {
      getWithDefault(name).map((s) => Try(s.toBoolean).getOrElse(invalid(name))) getOrElse (missing(name))
    }


    private def missing(name: String) =
      throw new RuntimeException(s"Parameter named ${name} was needed but missing")

    private def invalid(name: String) =
      throw new RuntimeException(s"Parameter named ${name} is invalid")


    def optionalParam(name: String): Option[String] =
      this getWithDefault (name)

    override def getAllParameters: util.Set[String] = provider().getAllParameters;
  }


  case class ShathelCommandContext(shathel: Shathel, provider: () => Parameters) extends Provider(provider)


}
