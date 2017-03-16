package org.s4s0l.shathel.deployer

import java.io.File
import java.util.Optional

import org.apache.commons.io.FileUtils
import org.eclipse.aether.artifact.{Artifact, DefaultArtifact}
import org.eclipse.aether.graph.{Dependency, Exclusion}
import org.eclipse.aether.repository.RemoteRepository
import org.eclipse.aether.util.graph.visitor.PreorderNodeListGenerator
import org.s4s0l.shathel.commons.core.Parameters
import org.s4s0l.shathel.commons.core.dependencies.{DependencyDownloader, StackLocator}
import org.s4s0l.shathel.commons.core.stack.StackReference
import org.s4s0l.shathel.commons.utils.IoUtils
import org.s4s0l.shathel.deployer.mvn.ShathelMavenRepository
import org.s4s0l.shathel.deployer.mvn.ShathelMavenRepository.ShathelMavenSettings
import org.s4s0l.shathel.deployer.mvn.ShathelMavenRepository.ShathelMavenSettings.ShathelMavenSettingsBuilder
import org.s4s0l.shathel.deployer.shell.customization.CustomBanner

import scala.collection.JavaConverters._

/**
  * @author Marcin Wielgus
  */
class MvnDependencyDownloader(parameters: Parameters) extends DependencyDownloader {
  val reg = "(([^:]+):)?([^:]+):?([^:]+)?".r


  def defaultVersion = CustomBanner.versionInfo()

  def defaultGroup = "org.s4s0l.shathel"

  override def download(locator: StackLocator, directory: File, forceful: Boolean): Optional[File] = {
    val reference: StackReference = if (locator.getReference.isPresent) {
      locator.getReference.get()
    } else {
      new StackReference(locator.getLocation match {
        case reg(_, null, name, null) => s"${defaultGroup}:${name}:${defaultVersion}"
        case reg(_, group, name, null) => s"${group}:${name}:${defaultVersion}"
        case reg(_, group, name, "$version") => s"${group}|${name}:${defaultVersion}"
        case reg(_, group, name, version) => s"${group}:${name}:${version}"
      })
    }


    val destDirectory = new File(directory, reference.getStackDirecctoryName)
    if (!forceful && destDirectory.exists()) {
      return Optional.of(destDirectory)
    }
    return downloadZipFile(reference, directory).map(it => Optional.of(it)).getOrElse(Optional.empty())


  }

  private def downloadZipFile(reference: StackReference, directory: File): Option[File] = {
    def getLocalRepoLocation = parameters.getParameter("shathel.mvn.localRepo")
      .orElseGet(() => ShathelMavenSettingsBuilder.getDefaultLocalRepo)


    def getM2SecuritySettingsLocation = parameters.getParameter("shathel.mvn.securitySetting")
      .orElseGet(() => ShathelMavenSettingsBuilder.getDefaultM2SecuritySetting)


    def getM2SettingsLocation = parameters.getParameter("shathel.mvn.settings")
      .orElseGet(() => ShathelMavenSettingsBuilder.getDefaultUserSettingsFile)


    def getRemoteRepository = new RemoteRepository.Builder(
      parameters.getParameter("shathel.mvn.repoId").orElseGet(() => "central"),
      "default",
      parameters.getParameter("shathel.mvn.repoUrl").orElseGet(() => ShathelMavenSettingsBuilder.getMavenCentrajUrl)
    ).build

    val instance = new ShathelMavenRepository(
      ShathelMavenSettings
        .builder()
        .localRepo(getLocalRepoLocation)
        .m2SecuritySetting(getM2SecuritySettingsLocation)
        .m2Settings(getM2SettingsLocation)
        .repository(getRemoteRepository)
        .build()
    )

    val node = instance.resolveDependency(
      new Dependency(
        new DefaultArtifact(s"${reference.getGroup}:${reference.getName}:zip:shathel:${reference.getVersion}")
        , "compile", false
        , List(new Exclusion("*", "*", "*", "jar")).asJava
      ))

    val nlg = new PreorderNodeListGenerator()
    node.accept(nlg)
    return nlg.getArtifacts(true).asScala
      .filter(_.getClassifier.equals("shathel"))
      .map((artifact: Artifact) => {
        FileUtils.copyFileToDirectory(artifact.getFile, directory)
        new File(directory, artifact.getFile.getName)
      })
      .map((file) => {
        val ff = new File(directory, reference.getStackDirecctoryName)
        IoUtils.unZipIt(file, ff)
        ff
      })
      .headOption;
  }

}
