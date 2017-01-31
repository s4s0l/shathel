package org.s4s0l.shathel.deployer

import java.io.File

import org.apache.commons.io.FileUtils
import org.eclipse.aether.artifact.{Artifact, DefaultArtifact}
import org.eclipse.aether.graph.{Dependency, Exclusion}
import org.eclipse.aether.repository.RemoteRepository
import org.eclipse.aether.util.graph.visitor.PreorderNodeListGenerator
import org.s4s0l.shathel.commons.core.Parameters
import org.s4s0l.shathel.commons.core.dependencies.DependencyDownloader
import org.s4s0l.shathel.commons.core.stack.StackReference
import org.s4s0l.shathel.deployer.mvn.ShathelMavenRepository
import org.s4s0l.shathel.deployer.mvn.ShathelMavenRepository.ShathelMavenSettings
import org.s4s0l.shathel.deployer.mvn.ShathelMavenRepository.ShathelMavenSettings.ShathelMavenSettingsBuilder

import scala.collection.JavaConverters._

/**
  * @author Matcin Wielgus
  */
class MvnDependencyDownloader(parameters: Parameters) extends DependencyDownloader {


  override def download(reference: StackReference, directory: File): Unit = {

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

    nlg.getArtifacts(true).asScala
      .filter(_.getClassifier.equals("shathel"))
      .foreach[Unit]((artifact: Artifact) => FileUtils.copyFileToDirectory(artifact.getFile, directory))

  }
}
