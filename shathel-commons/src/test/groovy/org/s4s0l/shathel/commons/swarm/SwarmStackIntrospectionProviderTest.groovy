package org.s4s0l.shathel.commons.swarm

import org.s4s0l.shathel.commons.Shathel
import org.s4s0l.shathel.commons.core.environment.StackIntrospection
import org.s4s0l.shathel.commons.core.stack.StackReference
import org.s4s0l.shathel.commons.docker.DockerWrapper
import spock.lang.Specification
import testutils.BaseIntegrationTest

/**
 * @author Marcin Wielgus
 */
class SwarmStackIntrospectionProviderTest extends BaseIntegrationTest {

    @Override
    def setupEnvironment() {
        environmentName = "local"
        network = "222.212.222"
    }


    def cleanupEnvironment() {
        new DockerWrapper().with {
            if (swarmActive()) {
                stackUnDeploy(new File("."), "intrDepName")
            }
        }
    }

    def "Introspection should provide proper labels and container info"() {
        given:
        Shathel sht = shathel()
        def solution = sht.getSolution(sht.initStorage(getRootDir(), false))
        def environment = solution.getEnvironment(environmentName)
        if (!environment.isInitialized()) {
            environment.initialize()
        }
        def reference = new StackReference("org.s4s0l.shathel:introspection:1.0")


        when:
        def stack = solution.openStack(environment, reference)
        stack.run(stack.createStartCommand(false))
        def introspection = environment.getIntrospectionProvider().getStackIntrospection(reference)

        then:
        introspection.get().reference == reference
        introspection.get().labels["org.shathel.stack.gav"] == reference.gav
        introspection.get().labels["org.shathel.stack.deployName"] == "intrDepName"
        introspection.get().labels["org.shathel.stack.ga"] == "${reference.getGroup()}:${reference.getName()}"
        introspection.get().labels["org.shathel.stack.marker"] == "true"


        introspection.get().services.size() == 1
        introspection.get().services.head().currentInstances == 2
        introspection.get().services.head().requiredInstances == 2
        introspection.get().services.head().fullServiceName == "intrDepName_dummyService"
        introspection.get().services.head().serviceName == "dummyService"
        introspection.get().services.head().getPort(4000) == 9999
        introspection.get().services.head().getPort(5000) != 5000
        introspection.get().services.head().getPort(9000) == 9000


        onEnd()

    }

}
