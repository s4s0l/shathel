package org.s4s0l.shathel.deployer

import java.io.File
import java.util
import java.util.Optional

import org.s4s0l.shathel.commons.Shathel
import org.s4s0l.shathel.commons.core.{CommonParams, Parameters}

import scala.util.Try

/**
  * @author Marcin Wielgus
  */
object DeployerParameters {
  val VALID_PARAMS = List(
    "shathel.env",
    "shathel.dir",
    "shathel.solution.name",
    "shathel.solution.file_default_version",
    "shathel.solution.file_default_group",
    "shathel.solution.git_default_version",
    "shathel.solution.git_default_group",
    "shathel.solution.ivy_default_version",
    "shathel.solution_ivy_default_group",
    "shathel.solution.ivy_settings",
    "shathel.solution.ivy_repo_id",
    "shathel.solution.ivy_repos",
    "shathel.env.{env}.net",
    "shathel.env.{env}.init",
    "shathel.env.{env}.registry",
    "shathel.env.{env}.managers",
    "shathel.env.{env}.forceful",
    "shathel.env.{env}.workers",
    "shathel.env.{env}.dependenciesDir",
    "shathel.env.{env}.dataDir",
    "shathel.env.{env}.safeDir",
    "shathel.env.{env}.settingsDir",
    "shathel.env.{env}.enrichedDir",
    "shathel.env.{env}.tempDir",
    "shathel.env.{env}.pull",
    "shathel.env.{env}.domain",
    "shathel.env.{env}.safePassword"
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
    CommonParams.SHATHEL_DIR -> ".shathel",
    "shathel.env.local.init" -> "true",
    CommonParams.SHATHEL_ENV -> "local"
  )

  class Builder(values: Map[String, String] = Map()) {

    def apply(param: (String, Option[String])) = new Builder(values + (param._1 -> param._2.getOrElse(values.getOrElse(param._1, null))));

    def storageFile(f: File): Builder = this (CommonParams.SHATHEL_DIR -> Option(f).map(_.getAbsolutePath))

    def environment(f: String): Builder = this (CommonParams.SHATHEL_ENV -> Option(f).map(_.toString))

    def forceful(f: java.lang.Boolean): Builder = this (s"shathel.env.${values.get(CommonParams.SHATHEL_ENV)}.forceful" -> Option(f).map(_.toString))

    def build(): Map[String, String] = values.filter(e => e._2 != null)
  }

  class Provider(provider: () => Parameters) extends Parameters {


    def storageFile(): File = new File(mandatoryParam(CommonParams.SHATHEL_DIR))

    def environment(): String = mandatoryParam(CommonParams.SHATHEL_ENV)

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

    def optionalBoolean(name: String): Option[Boolean] =
      getWithDefault(name).map((s) => Try(s.toBoolean).getOrElse(invalid(name)))

    override def getAllParameters: util.Set[String] = provider().getAllParameters;
  }


  case class ShathelCommandContext(shathel: Shathel, provider: () => Parameters) extends Provider(provider)


}
