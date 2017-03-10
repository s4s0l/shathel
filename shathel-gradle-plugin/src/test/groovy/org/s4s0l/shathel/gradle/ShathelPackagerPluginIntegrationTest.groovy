package org.s4s0l.shathel.gradle

import spock.lang.Specification

/**
 * @author Marcin Wielgus
 */
class ShathelPackagerPluginIntegrationTest extends org.s4s0l.bootcker.gradle.utils.GradlePluginFunctionalSpecification {

    def "basicTest"() {
        given:
        useProjectStructure "../sample-gradle-projects/simple-project"

        when:
        run 'install'

        then:
        noExceptionThrown()
        file("build/libs/simple-project-1.2.3-SNAPSHOT-shathel.zip").exists()

    }

    /**
     * This test needs to execute after
     * @see #basicTest()
     * @return
     */
    def "basicTest2"() {
        given:
        useProjectStructure "../sample-gradle-projects/simple-project2"

        when:
        run 'install'
        then:
        noExceptionThrown()
        file("build/libs/simple-project2-${System.getenv("PROJECT_VERSION")}-shathel.zip").exists()


        when:
        run 'shtCollectDependencies'
        then:
        noExceptionThrown()
        file("build/shtTemporary/dependencies/simple-project2-${System.getenv("PROJECT_VERSION")}-shathel.zip").exists()
        file("build/shtTemporary/dependencies/simple-project-1.2.3-SNAPSHOT-shathel.zip").exists()

    }
}
