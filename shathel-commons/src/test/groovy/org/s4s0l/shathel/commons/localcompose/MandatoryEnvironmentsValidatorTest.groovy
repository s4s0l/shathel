package org.s4s0l.shathel.commons.localcompose

import org.s4s0l.shathel.commons.Shathel
import org.s4s0l.shathel.commons.core.stack.StackReference
import org.s4s0l.shathel.commons.docker.DockerWrapper
import spock.lang.Specification
import testutils.BaseIntegrationTest

/**
 * @author Marcin Wielgus
 */
class MandatoryEnvironmentsValidatorTest extends BaseIntegrationTest {

    @Override
    def setupEnvironment() {
        environmentName = "local"
        network = "222.222.222"
        solutionDescription =
                """
version: 1
shathel-solution:
  name: playground
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

    def "Missing variables should be reported"() {
        given:
        Shathel sht = shathel()
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
        RuntimeException ex = thrown()
        ex.message == 'Missing env vars: MANDATORY_PARAM'

        onEnd()

    }

}
