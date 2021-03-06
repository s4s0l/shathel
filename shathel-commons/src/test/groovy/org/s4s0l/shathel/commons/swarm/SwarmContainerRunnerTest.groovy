package org.s4s0l.shathel.commons.swarm

import org.s4s0l.shathel.commons.Shathel
import org.s4s0l.shathel.commons.core.stack.StackReference
import org.s4s0l.shathel.commons.docker.DockerWrapper
import testutils.BaseIntegrationTest

/**
 * @author Marcin Wielgus
 */
class SwarmContainerRunnerTest extends BaseIntegrationTest {

    @Override
    def setupEnvironment() {
        environmentName = "local"
    }


    def cleanupEnvironment() {
        new DockerWrapper().with {
            if (swarmActive()) {
                stackUnDeploy(new File("."), "updateing")
            }
        }
    }

    def "Runner should wait for update to finish"() {
        given:
        Shathel sht = shathel()
        def solution = sht.getSolution(sht.initStorage(getRootDir(), false))
        def environment = solution.getEnvironment(environmentName)
        if (!environment.isInitialized()) {
            environment.initialize()
        }


        when:
        def stack = solution.openStack( new StackReference("org.s4s0l.shathel:updateing:1.0"))
        def command = stack.createStartCommand(false,environment)
        solution.run(command)

        then:
        ["1","1","1","1","1"] == execInAllTasks(environment, "updateing_service", "printenv CURRENT_VERSION")


        when:
        stack = solution.openStack( new StackReference("org.s4s0l.shathel:updateing:2.0"))
        command = stack.createStartCommand(false,environment)
        solution.run(command)

        then:
        ["2","2","2","2","2", "2"] == execInAllTasks(environment, "updateing_service", "printenv CURRENT_VERSION")

        onEnd()

    }

}
