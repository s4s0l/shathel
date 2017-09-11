package org.s4s0l.shathel.gradle
/**
 * @author Marcin Wielgus
 */
class ShathelPackagerPluginIntegrationTest extends org.s4s0l.bootcker.gradle.utils.GradlePluginFunctionalSpecification {

    def "Prepares docker context and shathel package"() {
        given:
        useProjectStructure "../sample-gradle-projects/simple-project"

        when:
        run "clean", "shathelPrepare"

        then:
        noExceptionThrown()
        file("build/shathel-dockers/dummyExtra/Dockerfile2").exists()
        file("build/shathel-dockers/simple-project/Dockerfile").exists()
        file("build/shathel-stacks/simple-project/shthl-stack.yml").exists()

        file("build/shathel-dockers/dummyExtra/Dockerfile2").text.contains("1.2.3-SNAPSHOT")
        file("build/shathel-dockers/simple-project/Dockerfile").text.contains("1.2.3-SNAPSHOT")
        //TODO parse yml and check throughl;y
        file("build/shathel-stacks/simple-project/shthl-stack.yml").text.contains("gav: org.s4s0l.shathel.gradle.sample:simple-project:1.2.3-SNAPSHOT")
        file("build/shathel-stacks/simple-project/shthl-stack.yml").text.contains("TOKEN_VALUE")
        file("build/shathel-stacks/simple-project/stack/docker-compose.yml").text.contains("context: ./dockerfiles/dummyExtra")
        file("build/shathel-stacks/simple-project/stack/docker-compose.yml").text.contains("context: ./dockerfiles/simple-project")
        file("build/shathel-stacks/simple-project/stack/docker-compose.yml").text.contains("dockerfile: Dockerfile2")
        file("build/shathel-stacks/simple-project/stack/docker-compose.yml").text.contains("TOKEN_VALUE")

        file("build/shathel-stacks/simple-project/stack/dockerfiles/dummyExtra/Dockerfile2").exists()
        file("build/shathel-stacks/simple-project/stack/dockerfiles/simple-project/Dockerfile").exists()

        file("build/shathel-stacks/org_s4s0l_shathel_gradle_sample_simple_project_1_2_3_SNAPSHOT").exists()

        when:
        run "shathelAssemble"

        then:
        file("build/libs/simple-project-1.2.3-SNAPSHOT-shathel.zip").exists()

        when:
        run "test"

        then:
        noExceptionThrown()

    }


}
