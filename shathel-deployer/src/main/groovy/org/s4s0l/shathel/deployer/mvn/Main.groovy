package org.s4s0l.shathel.deployer.mvn

import org.eclipse.aether.artifact.DefaultArtifact
import org.eclipse.aether.graph.Dependency
import org.eclipse.aether.util.graph.visitor.PreorderNodeListGenerator

/**
 * @author Matcin Wielgus
 */
class Main {

    static void mainXX(String[] args) {
        def instance = new ShathelMavenRepository(ShathelMavenRepository.ShathelMavenSettings.builder().localRepo("build/localrepo").build())
        def node = instance.resolveDependency(
                new Dependency(
                        new DefaultArtifact("org.s4s0l.shathel.gradle.sample2:basicTest2:zip:sht:DEVELOPER-SNAPSHOT")
,
                        null
                )
        )
        PreorderNodeListGenerator nlg = new PreorderNodeListGenerator();
        node.accept(nlg)
        nlg.getArtifacts(true).findAll {it.classifier == 'sht'}.forEach {
            println it.file
        }
    }

}