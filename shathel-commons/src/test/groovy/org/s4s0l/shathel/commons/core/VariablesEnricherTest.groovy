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
        solutionDescription =
                """
version: 1
shathel-solution:
  name: playground
  envs:
    FROM_SHATHEL_FILE_SOLUTION: FROM_SHATHEL_FILE_SOLUTION 
  variables:
    envs:
      FROM_SHATHEL_FILE_STACK: FROM_SHATHEL_FILE_STACK
      MANDATORY_PARAM: MANDATORY_PARAM
  environments:
    local:
      type: local-swarm
      build-allowed: true
      domain: localhost
"""
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
        Shathel sht = shathel(["shathel.env.${environmentName}.domain":"mydomain.com"])
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
        "1" == execInAnyTask(environment, "variables_service", "printenv ENV_SIZE")
        "1" == execInAnyTask(environment, "variables_service", "printenv ENV_QUORUM")
        "1" == execInAnyTask(environment, "variables_service", "printenv ENV_MGM_SIZE")
        "1" == execInAnyTask(environment, "variables_service", "printenv ENV_MGM_QUORUM")
        "FROM_SHATHEL_FILE_STACK" == execInAnyTask(environment, "variables_service", "printenv FROM_SHATHEL_FILE_STACK")
        "FROM_SHATHEL_FILE_SOLUTION" == execInAnyTask(environment, "variables_service", "printenv FROM_SHATHEL_FILE_SOLUTION")
        "mydomain.com" == execInAnyTask(environment, "variables_service", "printenv DNS")

        onEnd()

    }

}
