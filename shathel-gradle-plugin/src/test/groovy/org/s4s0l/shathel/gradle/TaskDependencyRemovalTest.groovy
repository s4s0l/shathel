package org.s4s0l.shathel.gradle


import org.gradle.api.Task
import org.gradle.api.artifacts.Dependency
import org.gradle.api.tasks.TaskDependency
import org.gradle.api.tasks.bundling.Zip
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification

/**
 * @author Marcin Wielgus
 */
class TaskDependencyRemovalTest extends Specification {

    def "it could be possible to apply plugin"() {
        def project = ProjectBuilder.builder().build().with {
            apply plugin: 'maven'
            def x = task("bootRepackage")

            x.dependsOn(
                    project.getConfigurations().getByName(Dependency.ARCHIVES_CONFIGURATION)
                            .getAllArtifacts().getBuildDependencies());

            def y = task("yyyy", type: Zip) {
                destinationDir new File(project.buildDir, 'libs')
                from "src"
            }

            project.artifacts {
                archives y
            }
            project.tasks.matching {it.name == "bootRepackage"}.each {
                Set<Object> deps =  it.taskDependencies.values
                def toBeeRemoved = deps.find {
                    it.class.name.startsWith("org.gradle.api.internal.artifacts.DefaultPublishArtifactSet")
                }
                deps.removeAll(toBeeRemoved)
            }

            project
        }
        when:
        def dependencies = project.tasks.getByName("bootRepackage").taskDependencies
        def deps = dependencies.getDependencies(project.tasks.getByName("bootRepackage"))
        then:
        deps.size() == 0
    }
}
