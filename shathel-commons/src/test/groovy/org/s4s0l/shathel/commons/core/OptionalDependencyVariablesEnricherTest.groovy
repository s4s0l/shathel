package org.s4s0l.shathel.commons.core

import org.s4s0l.shathel.commons.Shathel
import org.s4s0l.shathel.commons.core.stack.StackReference
import org.s4s0l.shathel.commons.docker.DockerWrapper
import org.s4s0l.shathel.commons.utils.ExecWrapper
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
        def stack = solution.openStack( new StackReference("org.s4s0l.shathel:dependency:1.0"))
        def command = stack.createStartCommand(false,environment)

        then:
        /**
         * dependency
         *    |
         *    |-> dependency2
         *    |-> dependency3
         */
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
        stack = solution.openStack( new StackReference("org.s4s0l.shathel:dependency1:1.0"))
        command = stack.createStartCommand(false,environment)

        then:
        /**
         * dependency
         *    |
         *    |-> dependency1 (*) (new)
         *    |      |
         *    |      |-> dependency2
         *    |      |-> dependency3 (*)
         *    |-> dependency2
         *    |-> dependency3
         */
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
        command = stack.createStopCommand(true, false,environment)

        then:
        /**
         * dependency
         *    |
         *    |-> dependency1 (*) (to bee deleted)
         *    |      |
         *    |      |-> dependency2
         *    |      |-> dependency3 (*)
         *    |-> dependency2
         *    |-> dependency3
         */
        command.commands.size() == 1

        when:
        command = stack.createStopCommand(true, true,environment)

        then:
        /**
         * dependency
         *    |
         *    |-> dependency1 (*) (to bee deleted)
         *    |      |
         *    |      |-> dependency2
         *    |      |-> dependency3 (*)
         *    |-> dependency2
         *    |-> dependency3
         */
        command.commands.size() == 1

        when:
        stack = solution.openStack( new StackReference("org.s4s0l.shathel:dependency:1.0"))
        command = stack.createStopCommand(false, false,environment)

        then:
        /**
         * dependency  (to bee deleted)
         *    |
         *    |-> dependency1 (*)
         *    |      |
         *    |      |-> dependency2
         *    |      |-> dependency3 (*)
         *    |-> dependency2
         *    |-> dependency3
         */
        command.commands.size() == 1

        when:
        command = stack.createStopCommand(true, false,environment)

        then:
        /**
         * dependency  (to bee deleted)
         *    |
         *    |-> dependency1 (*)
         *    |      |
         *    |      |-> dependency2
         *    |      |-> dependency3 (*)
         *    |-> dependency2
         *    |-> dependency3
         */
        command.commands.size() == 1

        when:
        command = stack.createStopCommand(true, true,environment)

        then:
        /**
         * dependency  (to bee deleted)
         *    |
         *    |-> dependency1 (*) (to bee deleted)
         *    |      |
         *    |      |-> dependency2 (to bee deleted)
         *    |      |-> dependency3 (*) (to bee deleted)
         *    |-> dependency2
         *    |-> dependency3
         */
        command.commands.size() == 4

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
        def stack = solution.openStack( new StackReference("org.s4s0l.shathel:dependency1:1.0"))
        def command = stack.createStartCommand(true,environment)

        then:
        /**
         *    |-> dependency1 (*)
         *    |      |
         *    |      |-> dependency2
         *    |      |-> dependency3 (*)
         *    |      |-> dependency4 (*)
         */
        command.commands.size() == 4

        when:
        solution.run(command)


        then:
        "present" == execInAnyTask(environment, "dependency1_service", "printenv DEPENDENCY2")
        "present" == execInAnyTask(environment, "dependency1_service", "printenv DEPENDENCY3")
        "present" == execInAnyTask(environment, "dependency1_service", "printenv DEPENDENCY4")

        environment.getIntrospectionProvider().allStacks.stacks.size() == 4

        when:
        stack = solution.openStack( new StackReference("org.s4s0l.shathel:dependency:1.0"))
        command = stack.createStartCommand(true,environment)

        then:
        /**
         *
         * dependency (new)
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
        command.commands.size() == 1

        when:
        solution.run(command)

        then:
        "present" == execInAnyTask(environment, "dependency_service", "printenv DEPENDENCY1")
        "present" == execInAnyTask(environment, "dependency_service", "printenv DEPENDENCY2")
        "present" == execInAnyTask(environment, "dependency_service", "printenv DEPENDENCY3")
        "present" == execInAnyTask(environment, "dependency_service", "printenv DEPENDENCY4")
        environment.getIntrospectionProvider().allStacks.stacks.size() == 5


        when:
        stack = solution.openStack(new StackReference("org.s4s0l.shathel:dependency:1.0"))
        command = stack.createStopCommand(true, false, environment)

        then:
        /**
         * dependency (to remove)
         *    |
         *    |-> dependency1 (*)
         *    |      |
         *    |      |-> dependency2
         *    |      |-> dependency3 (*)
         *    |      |-> dependency4 (*)
         *    |-> dependency2
         *    |-> dependency3 (not removed as needed by 1)
         *    |-> dependency4 (*)
         */
        command.commands.size() == 1

        when:
        command = stack.createStopCommand(true, true, environment)

        then:
        /**
         * dependency (to remove)
         *    |
         *    |-> dependency1 (*) (to remove)
         *    |      |
         *    |      |-> dependency2
         *    |      |-> dependency3 (*)
         *    |      |-> dependency4 (*)
         *    |-> dependency2 (to remove)
         *    |-> dependency3 (to remove)
         *    |-> dependency4 (*) (to remove)
         */
        command.commands.size() == 5


        when:
        stack = solution.openStack(new StackReference("org.s4s0l.shathel:dependency1:1.0"))
        command = stack.createStopCommand(true, true, environment)

        then:
        /**
         * dependency
         *    |
         *    |-> dependency1 (*) (to remove)
         *    |      |
         *    |      |-> dependency2
         *    |      |-> dependency3 (*)
         *    |      |-> dependency4 (*) (to remove)
         *    |-> dependency2
         *    |-> dependency3
         *    |-> dependency4 (*)
         */
        command.commands.size() == 2


        onEnd()

    }

}
