package org.s4s0l.shathel.commons

import org.eclipse.aether.artifact.DefaultArtifact
import org.eclipse.aether.graph.Dependency
import org.eclipse.aether.util.graph.visitor.PreorderNodeListGenerator

/**
 * @author Matcin Wielgus
 */
class Main {

    static void main(String[] args) {
        def instance = new ShathelMavenRepository()
        def node = instance.resolveDependency(
                new Dependency(
                        new DefaultArtifact("org.apache.maven:maven-profile:2.1.0"),
                        "compile"
                )
        )
        PreorderNodeListGenerator nlg = new PreorderNodeListGenerator();
        node.accept(nlg);
        nlg.getArtifacts(true).forEach {
            println it.file
        }
    }

}