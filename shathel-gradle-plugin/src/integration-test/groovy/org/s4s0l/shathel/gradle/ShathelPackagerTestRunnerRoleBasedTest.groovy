package org.s4s0l.shathel.gradle
/**
 * @author Marcin Wielgus
 */
class ShathelPackagerTestRunnerRoleBasedTest extends org.s4s0l.bootcker.gradle.utils.GradlePluginFunctionalSpecification {

    def "Role based testing  "() {
        given:
        useProjectStructure "../sample-gradle-projects/role-project"

        when:
        run "test"

        then:
        noExceptionThrown()

    }


}
