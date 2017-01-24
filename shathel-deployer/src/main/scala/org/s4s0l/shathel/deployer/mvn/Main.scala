package org.s4s0l.shathel.deployer.mvn

import java.util.function.{Consumer, Predicate}

import org.eclipse.aether.artifact.{Artifact, DefaultArtifact}
import org.eclipse.aether.graph.Dependency
import org.eclipse.aether.graph.Exclusion
import org.eclipse.aether.util.graph.visitor.PreorderNodeListGenerator

import scala.collection.JavaConverters._

/**
  * @author Matcin Wielgus
  */
object Main {
  def mains(args: Array[String]) : Unit = {
    val instance = new ShathelMavenRepository(ShathelMavenRepository.ShathelMavenSettings.builder().localRepo("build/localrepo").build())

    val node = instance.resolveDependency(
      new Dependency(
        new DefaultArtifact("org.s4s0l.shathel.gradle.sample2:simple-project2:zip:shathel:DEVELOPER-SNAPSHOT")
        , "compile", false
        , List(new Exclusion("*", "*", "*", "jar")).asJava
      ))

    val nlg = new PreorderNodeListGenerator()
    node.accept(nlg)

    nlg.getArtifacts(true)
      .stream()
      .filter(_.getClassifier.equals("shathel"))
      .forEach((t: Artifact) => println(t.getFile.getAbsolutePath))

  }
}
