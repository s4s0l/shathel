package org.s4s0l.shathel.commons.core

import org.s4s0l.shathel.commons.Shathel
import org.s4s0l.shathel.commons.core.stack.StackReference
import org.s4s0l.shathel.commons.docker.DockerWrapper
import spock.lang.Specification
import testutils.BaseIntegrationTest

/**
 * @author Marcin Wielgus
 */
class VariablesEnricherTest extends BaseIntegrationTest {

    @Override
    def setupEnvironment() {
        environmentName = "local"
        network = "222.222.222"
    }


    def cleanupEnvironment() {
        new DockerWrapper().with {
            if (swarmActive()) {
                stackUnDeploy(new File("."), "variables")
            }
        }
    }

    def "Variables should be exposed to Stack compose"() {
        given:
        Shathel sht = new Shathel(prepare())
        def solution = sht.getSolution(sht.initStorage(getRootDir(), false))
        def environment = solution.getEnvironment(environmentName)
        if (!environment.isInitialized()) {
            environment.initialize()
        }


        when:
        def stack = solution.openStack(environment, new StackReference("org.s4s0l.shathel:variables:1.0"))
        def command = stack.createStartCommand(false)
        stack.run(command)


        then:
        waitForService(environment, "variables_service")
        "1" == execInAnyTask(environment, "variables_service", "printenv ENV_SIZE")
        "1" == execInAnyTask(environment, "variables_service", "printenv ENV_QUORUM")
        "1" == execInAnyTask(environment, "variables_service", "printenv ENV_MGM_SIZE")
        "1" == execInAnyTask(environment, "variables_service", "printenv ENV_MGM_QUORUM")

        onEnd()

    }

}
