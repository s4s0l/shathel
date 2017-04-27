package org.s4s0l.shathel.gradle
/**
 * @author Marcin Wielgus
 */
class ShathelPackagerRemoteEnvTest extends org.s4s0l.bootcker.gradle.utils.GradlePluginFunctionalSpecification {

    def "Remote env testing"() {
        given:
        useProjectStructure "../sample-gradle-projects/multi-project-vbox"

        when:
        run "clean", "test", "integrationTest", "shathelDestroy"

        then:
        noExceptionThrown()

    }


}
