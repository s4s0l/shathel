package org.s4s0l.shathel.commons.secrets

import org.s4s0l.shathel.commons.Shathel
import org.s4s0l.shathel.commons.core.Solution
import org.s4s0l.shathel.commons.core.Stack
import org.s4s0l.shathel.commons.core.StackOperations
import org.s4s0l.shathel.commons.core.environment.Environment
import org.s4s0l.shathel.commons.core.stack.StackReference
import org.s4s0l.shathel.commons.docker.DockerWrapper
import spock.lang.Specification
import testutils.BaseIntegrationTest

/**
 * @author Marcin Wielgus
 */
class SecretManagerTest
        extends BaseIntegrationTest {

    @Override
    def setupEnvironment() {
        environmentName = "local"
        network = "222.222.222"
    }


    def cleanupEnvironment() {
        new DockerWrapper().with {
            if (swarmActive()) {
                stackUnDeploy(new File("."), "secret-consumer")
                stackUnDeploy(new File("."), "secret-provider")
                secretRemove("shathel_some_pass_1")
                secretRemove("shathel_some_pass_2")
            }
        }
    }

    def "Secret updates should restart dependant services"() {
        given:
        Shathel sht = shathel()
        def solution = sht.getSolution(sht.initStorage(getRootDir(), false))
        def environment = solution.getEnvironment(environmentName)
        if (!environment.isInitialized()) {
            environment.initialize()
        }


        when:
        def stack = solution.openStack(environment, new StackReference("org.s4s0l.shathel:secret-consumer:1.0"))
        def command = stack.createStartCommand(false)
        stack.run(command)


        then:
        environment.getIntrospectionProvider().allStacks.size() == 2
        "password" == execInAnyTask(environment, "secret-consumer_service", "cat /run/secrets/consumer_password")
        "password" == execInAnyTask(environment, "secret-provider_service", "cat /run/secrets/shathel_some_pass")
        "password2" == execInAnyTask(environment, "secret-provider_service", "cat /run/secrets/donottouch")

        when:
        System.setProperty("shathel.env.local.shathel_some_pass_secret_value", "dummy")
        environment.environmentApiFacade.secretManager.secretUpdate("shathel_some_pass", null)


        then:
        "dummy" == execInAnyTask(environment, "secret-consumer_service", "cat /run/secrets/consumer_password")
        "dummy" == execInAnyTask(environment, "secret-provider_service", "cat /run/secrets/shathel_some_pass")
        "password2" == execInAnyTask(environment, "secret-provider_service", "cat /run/secrets/donottouch")


        //AFTER RESTART LAST PASSWORD WILL BE USED


        when:
        def command2 = stack.createStopCommand(true, true)
        stack.run(command2)

        then:
        environment.getIntrospectionProvider().allStacks.size() == 0


        when:
        //because swarm has lag in destroying networks
        Thread.sleep(10000)
        command = stack.createStartCommand(false)
        stack.run(command)


        then:
        environment.getIntrospectionProvider().allStacks.size() == 2
        "dummy" == execInAnyTask(environment, "secret-consumer_service", "cat /run/secrets/consumer_password")
        "dummy" == execInAnyTask(environment, "secret-provider_service", "cat /run/secrets/shathel_some_pass")
        "password2" == execInAnyTask(environment, "secret-provider_service", "cat /run/secrets/donottouch")

        when:
        def servicesUsing = environment.environmentApiFacade.secretManager.getServicesUsingSecret("shathel_some_pass")
        def allSecretNames = environment.environmentApiFacade.secretManager.getAllSecretNames("shathel_some_pass")
        def currentName = environment.environmentApiFacade.secretManager.secretCurrentName("shathel_some_pass")

        then:
        ["secret-consumer_service","secret-provider_service"] == servicesUsing
        ["shathel_some_pass_2","shathel_some_pass_1"] == allSecretNames
        "shathel_some_pass_2" == currentName

        onEnd()


    }

}
