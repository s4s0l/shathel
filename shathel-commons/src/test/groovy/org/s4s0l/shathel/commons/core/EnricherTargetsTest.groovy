package org.s4s0l.shathel.commons.core

import org.s4s0l.shathel.commons.Shathel
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
class EnricherTargetsTest extends BaseIntegrationTest {

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
        def command = stack.createStartCommand(false,environment)

        then:
        command.commands.size() == 1
        command.commands[0].enricherPreProvisioners.collect {it.name} == ["enricher3-self"]

        when:
        solution.run(command)
        stack = solution.openStack( new StackReference("org.s4s0l.shathel:enricher1:1.0"))
        command = stack.createStartCommand(false,environment)

        then:
        command.commands.size() == 2
        command.commands[0].enricherPreProvisioners.collect { it.name } ==
                ["enricher2-self", "enricher1-all", "enricher3-all", "enricher3-allothers"]
        command.commands[1].enricherPreProvisioners.collect {it.name} == ["enricher2-all", "enricher2-deps", "enricher2-allothers", "enricher1-self", "enricher3-all", "enricher3-allothers"]


        onEnd()

    }

}
