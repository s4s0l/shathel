package org.s4s0l.shathel.commons.oldies

import org.apache.commons.io.FileUtils
import org.s4s0l.shathel.commons.Shathel
import org.s4s0l.shathel.commons.core.MapParameters
import org.s4s0l.shathel.commons.core.Parameters
import org.s4s0l.shathel.commons.core.stack.StackReference
import org.s4s0l.shathel.commons.docker.DockerWrapper
import org.s4s0l.shathel.commons.utils.IoUtils
import spock.lang.Specification
import testutils.BaseIntegrationTest

/**
 * @author Marcin Wielgus
 */
@Deprecated
class DindEnvironmentTest extends BaseIntegrationTest {
    @Override
    def setupEnvironment() {
        environmentName = "dind"
        network = "223.223.223"
    }

    def cleanupEnvironment() {
        new DockerWrapper().with {
            containerRemoveIfPresent("DindEnvironmentTest-${environmentName}-manager-1")
            containerRemoveIfPresent("DindEnvironmentTest-${environmentName}-manager-2")
            containerRemoveIfPresent("DindEnvironmentTest-${environmentName}-worker-1")
            if (networkExistsByFilter("name=shathel-dindenvironmenttest-${environmentName}"))
                networkRemove("shathel-dindenvironmenttest-${environmentName}")
        }
        true
    }

    def "Run stack in local docker integration test"() {
        given:
        File root = getRootDir()
        def wrapper = new DockerWrapper()
        Shathel sht = shathel([
                "shathel.env.${environmentName}.pull"    : "false",
                "shathel.env.${environmentName}.type"    : "dind",
                "shathel.env.${environmentName}.managers": "2",
                "shathel.env.${environmentName}.workers" : "1",
                "shathel.env.${environmentName}.net"     : "33.33.33",
                "shathel.env.${environmentName}.domain"  : "33.33.33.99",
        ])

        when:
        def storage = sht.initStorage(root, true)

        then:
        new File(root, "shathel-solution.yml").text.contains("name: ${getClass().getSimpleName()}")

        when:
        def solution = sht.getSolution(storage)
        def environment = solution.getEnvironment(environmentName)

        then:
        environment != null

        when:
        if (!environment.isInitialized()) {
            environment.initialize()
        }
        if (!environment.isStarted()) {
            environment.start()
        }
        then:
        wrapper.networkExistsByFilter("name=shathel-dindenvironmenttest-${environmentName}")
        wrapper.containerRunning("DindEnvironmentTest-${environmentName}-manager-1")
        wrapper.containerRunning("DindEnvironmentTest-${environmentName}-manager-2")
        wrapper.containerRunning("DindEnvironmentTest-${environmentName}-worker-1")

        when:
        def stack = solution.openStack(environment, new StackReference("test.group:dummy:2.0"))
        stack.run(stack.createStartCommand(false))

        then:
        environment.getIntrospectionProvider().allStacks.size() == 2

        onEnd()

    }


}
