package org.s4s0l.shathel.commons.core

import org.s4s0l.shathel.commons.Shathel
import org.s4s0l.shathel.commons.core.stack.StackReference
import org.s4s0l.shathel.commons.docker.DockerWrapper
import org.s4s0l.shathel.commons.utils.ExecWrapper
import spock.lang.Specification
import testutils.BaseIntegrationTest

/**
 *
 * dependency
 *    |
 *    |-> dependency1 (*)
 *    |      |
 *    |      |-> dependency2
 *    |      |-> dependency3 (*)
 *    |      |-> dependency4 (*)
 *    |-> dependency2
 *    |-> dependency3
 *    |-> dependency4 (*)
 * @author Marcin Wielgus
 */
class OptionalDependencyVariablesEnricherTest extends BaseIntegrationTest {

    @Override
    def setupEnvironment() {
        environmentName = "local"
    }


    def cleanupEnvironment() {
        new DockerWrapper().with {
            if (swarmActive()) {
                stackUnDeploy(new File("."), "dependency")
                stackUnDeploy(new File("."), "dependency1")
                stackUnDeploy(new File("."), "dependency2")
                stackUnDeploy(new File("."), "dependency3")
                stackUnDeploy(new File("."), "dependency4")
            }
        }
    }

    def "Optional dependencies should not be started"() {
        given:
        Shathel sht = shathel()
        def solution = sht.getSolution(sht.initStorage(getRootDir(), false))
        def environment = solution.getEnvironment(environmentName)
        if (!environment.isInitialized()) {
            environment.initialize()
        }


        when:
        def stack = solution.openStack(environment, new StackReference("org.s4s0l.shathel:dependency:1.0"))
        def command = stack.createStartCommand(false)

        then:
        command.commands.size() == 3

        when:
        solution.run(command)


        then:
        "absent" == execInAnyTask(environment, "dependency_service", "printenv DEPENDENCY1")
        "present" == execInAnyTask(environment, "dependency_service", "printenv DEPENDENCY2")
        "present" == execInAnyTask(environment, "dependency_service", "printenv DEPENDENCY3")
        "absent" == execInAnyTask(environment, "dependency_service", "printenv DEPENDENCY4")

        environment.getIntrospectionProvider().allStacks.stacks.size() == 3

        when:
        //But when some optional dependency is already running it should be reflected in env vars
        stack = solution.openStack(environment, new StackReference("org.s4s0l.shathel:dependency1:1.0"))
        command = stack.createStartCommand(false)

        then:
        command.commands.size() == 1

        when:
        solution.run(command)

        then:
        "present" == execInAnyTask(environment, "dependency1_service", "printenv DEPENDENCY2")
        "present" == execInAnyTask(environment, "dependency1_service", "printenv DEPENDENCY3")
        "absent" == execInAnyTask(environment, "dependency1_service", "printenv DEPENDENCY4")

        when:
        execInAnyTask(environment, "dependency1_service", "printenv DEPENDENCY1")
        then:
        thrown ExecWrapper.ExecWrapperException


        when:
        command = stack.createStopCommand(true, false)

        then:
        command.commands.size() == 2 //todo: it should be only one because dependency2 is used by root stack...

        when:
        command = stack.createStopCommand(true, true)

        then:
        command.commands.size() == 4 //todo: same as above should be 1 also it shows it includes nonexistent stacks

        when:
        stack = solution.openStack(environment, new StackReference("org.s4s0l.shathel:dependency:1.0"))
        command = stack.createStopCommand(false, false)

        then:
        command.commands.size() == 1

        when:
        command = stack.createStopCommand(true, false)

        then:
        command.commands.size() == 3 //todo: should be 1 because 2&3 are still needed by dependency1

        when:
        command = stack.createStopCommand(true, true)

        then:
        command.commands.size() == 5

        when:
        solution.run(command)

        then:
        environment.getIntrospectionProvider().allStacks.stacks.size() == 0

        onEnd()

    }


    def "Optional dependencies should be started"() {
        given:
        Shathel sht = shathel()
        def solution = sht.getSolution(sht.initStorage(getRootDir(), false))
        def environment = solution.getEnvironment(environmentName)
        if (!environment.isInitialized()) {
            environment.initialize()
        }


        when:
        def stack = solution.openStack(environment, new StackReference("org.s4s0l.shathel:dependency1:1.0"))
        def command = stack.createStartCommand(true)

        then:
        command.commands.size() == 4

        when:
        solution.run(command)


        then:
        "present" == execInAnyTask(environment, "dependency1_service", "printenv DEPENDENCY2")
        "present" == execInAnyTask(environment, "dependency1_service", "printenv DEPENDENCY3")
        "present" == execInAnyTask(environment, "dependency1_service", "printenv DEPENDENCY4")

        environment.getIntrospectionProvider().allStacks.stacks.size() == 4

        when:
        stack = solution.openStack(environment, new StackReference("org.s4s0l.shathel:dependency:1.0"))
        command = stack.createStartCommand(true)

        then:
        command.commands.size() == 1

        when:
        solution.run(command)

        then:
        "present" == execInAnyTask(environment, "dependency_service", "printenv DEPENDENCY1")
        "present" == execInAnyTask(environment, "dependency_service", "printenv DEPENDENCY2")
        "present" == execInAnyTask(environment, "dependency_service", "printenv DEPENDENCY3")
        "present" == execInAnyTask(environment, "dependency_service", "printenv DEPENDENCY4")
        environment.getIntrospectionProvider().allStacks.stacks.size() == 5

        onEnd()

    }

}
