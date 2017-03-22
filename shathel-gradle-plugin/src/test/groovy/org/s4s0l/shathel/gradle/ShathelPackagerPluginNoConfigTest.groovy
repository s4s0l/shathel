package org.s4s0l.shathel.gradle
/**
 * @author Marcin Wielgus
 */
class ShathelPackagerPluginNoConfigTest extends org.s4s0l.bootcker.gradle.utils.GradlePluginFunctionalSpecification {

    def "No Config - Prepares docker context and shathel package"() {
        given:
        useProjectStructure "../sample-gradle-projects/basic-project"

        when:
        run "clean", "shathelPrepare"

        then:
        noExceptionThrown()
        file("build/shathel-stack/shthl-stack.yml").exists()
        file("build/shathel-stack/shthl-stack.yml").text.contains("gav: org.s4s0l.shathel.gradle.sample:basic-project:1.0-SNAPSHOT")
        !file("build/shathel-stack/stack/dockerfiles/basic-project/Dockerfile").exists()
        file("build/shathel-mappings/org_s4s0l_shathel_gradle_sample_basic_project_1_0_SNAPSHOT").exists()

        when:
        run "shathelAssemble"

        then:
        file("build/libs/basic-project-1.0-SNAPSHOT-shathel.zip").exists()

        when:
        run "shathelDockerBuild"

        then:
        file("build/shathel-dockers/basic-project/Dockerfile").exists()

    }


}
