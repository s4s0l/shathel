package org.s4s0l.shathel.gradle
/**
 * @author Marcin Wielgus
 */
class ShathelPackagerMultiprojectTest extends org.s4s0l.bootcker.gradle.utils.GradlePluginFunctionalSpecification {

    def "Multiproject Prepares docker context and shathel package"() {
        given:
        useProjectStructure "../sample-gradle-projects/multi-project"

        when:
        run "clean", "shathelPrepare"

        then:
        noExceptionThrown()
        file("build/shathel-stacks/project1/shthl-stack.yml").text.contains("org.s4s0l.shathel.gradle.sample:project1:2.0")
        file("build/shathel-stacks/project1/stack/docker-compose.yml").text.contains("context: ./dockerfiles/project1")
        file("project1/build/shathel-dockers/project1/Dockerfile").text.contains("2.0")
        file("build/shathel-stacks/org_s4s0l_shathel_gradle_sample_project1_2_0").exists()


        file("build/shathel-stacks/project2/shthl-stack.yml").text.contains("org.s4s0l.shathel.gradle.sample:project2:2.0")
        file("build/shathel-stacks/project2/stack/docker-compose.yml").text.contains("context: ./dockerfiles/project1")
        file("build/shathel-stacks/project2/stack/docker-compose.yml").text.contains("context: ./dockerfiles/project2")
        file("project2/build/shathel-dockers/project2/Dockerfile").text.contains("2.0")
        file("build/shathel-stacks/org_s4s0l_shathel_gradle_sample_project2_2_0").exists()

        file("build/shathel-stacks/project0/shthl-stack.yml").text.contains("org.s4s0l.shathel.gradle.sample:project1:2.0")
        file("build/shathel-stacks/project0/shthl-stack.yml").text.contains("org.s4s0l.shathel.gradle.sample:project2:2.0")

        when:
        run "assemble"

        then:
        file("project1/build/libs/project1-2.0-shathel.zip").exists()
        file("project2/build/libs/project2-2.0-shathel.zip").exists()
        file("project0/build/libs/project0-2.0-shathel.zip").exists()

        when:
        run "test"

        then:
        noExceptionThrown()

    }


}
