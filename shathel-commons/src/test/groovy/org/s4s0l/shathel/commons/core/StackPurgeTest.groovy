package org.s4s0l.shathel.commons.core

import org.s4s0l.shathel.commons.Shathel
import org.s4s0l.shathel.commons.core.environment.StackCommand
import org.s4s0l.shathel.commons.core.stack.StackReference
import org.s4s0l.shathel.commons.docker.DockerWrapper
import testutils.BaseIntegrationTest

/**
 *
 * enricher1
 *    |
 *    |-> enricher2
 * enricher3
 * @author Marcin Wielgus
 */
class StackPurgeTest extends BaseIntegrationTest {

    @Override
    def setupEnvironment() {
        environmentName = "local"
    }


    def cleanupEnvironment() {
        new DockerWrapper().with {
            if (swarmActive()) {
                stackUnDeploy(new File("."), "enricher1")
                stackUnDeploy(new File("."), "enricher2")
                stackUnDeploy(new File("."), "enricher3")
            }
        }
    }

    def "Enrichers should be applied in proper targets"() {
        given:
        Shathel sht = shathel()
        def solution = sht.getSolution(sht.initStorage(getRootDir(), false))
        def environment = solution.getEnvironment(environmentName)
        if (!environment.isInitialized()) {
            environment.initialize()
        }


        when:

        def stack = solution.openStack(new StackReference("org.s4s0l.shathel:enricher3:1.0"))
        def command = stack.createStartCommand(false, environment)
        solution.run(command)
        stack = solution.openStack(new StackReference("org.s4s0l.shathel:enricher1:1.0"))
        command = stack.createStartCommand(false, environment)
        solution.run(command)

        then:
        environment.introspectionProvider.allStacks.rootStacks.size() == 2

        when:
        def purgeCommand = solution.getPurgeCommand(environment)
        def names = purgeCommand.commands.collect { it.description.name }
        then:
        purgeCommand.commands.collectEntries {
            [(it.description.name): it.type]
        } == ['enricher1', 'enricher2', 'enricher3'].collectEntries {
            [(it): StackCommand.Type.STOP]
        }
        //1 is dependant on 2 so 2 cannot be stopped first
        names.indexOf("enricher1") < names.indexOf("enricher2")

        when:
        solution.run(purgeCommand)

        then:
        environment.introspectionProvider.allStacks.size() == 0

        onEnd()

    }

}
