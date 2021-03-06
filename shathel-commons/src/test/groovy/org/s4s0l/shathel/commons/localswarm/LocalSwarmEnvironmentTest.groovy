package org.s4s0l.shathel.commons.localswarm

import org.apache.commons.io.FileUtils
import org.s4s0l.shathel.commons.Shathel
import org.s4s0l.shathel.commons.core.MapParameters
import org.s4s0l.shathel.commons.core.Parameters
import org.s4s0l.shathel.commons.core.environment.StackCommand
import org.s4s0l.shathel.commons.core.stack.StackReference
import org.s4s0l.shathel.commons.docker.DockerWrapper
import org.s4s0l.shathel.commons.utils.IoUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.yaml.snakeyaml.Yaml
import spock.lang.Specification
import testutils.BaseIntegrationTest

/**
 * @author Marcin Wielgus
 */
class LocalSwarmEnvironmentTest extends BaseIntegrationTest {

    @Override
    def setupEnvironment() {
        environmentName = "local"
    }


    def cleanupEnvironment() {
        new DockerWrapper().with {
            if (swarmActive()) {

                stackUnDeploy(new File("."), "sidekick")
                stackUnDeploy(new File("."), "dummy")
                //docker stack undeploy is async sometimes it cant stop the network therefore
                (0..10).forEach {
                    try {
                        stackUnDeploy(new File("."), "00shathel")
                    } catch (Exception e) {
                        Thread.sleep(5000)
                    }
                }
                stackUnDeploy(new File("."), "00shathel")
            }
        }
    }

    def "Run stack in local swarm integration test"() {
        given:
        File root = getRootDir()
        Shathel sht = shathel()

        when:
        def storage = sht.initStorage(root, true)

        then:
        new File(root, "shathel-solution.yml").text.contains("name: ${getClass().getSimpleName()}")


        when:
        def solution = sht.getSolution(storage)
        def environment = solution.getEnvironment("local")
        if(!environment.isInitialized()){
            environment.initialize()
        }

        then:
        environment.isInitialized()

        //        SIDEKICK INSTALLATION
        when:
        def stack = solution.openStack( new StackReference("org.s4s0l.shathel:sidekick:1.0"))
        def command = stack.createStartCommand(false,environment)
        solution.run(command)


        then:
        command.commands.size() == 1
        environment.getIntrospectionProvider().allStacks.size() == 1
        //        SIDEKICK INSTALLATION

        when:
        stack = solution.openStack( new StackReference("test.group:dummy:2.0"))
        command = stack.createStartCommand(false,environment);

        then:
        stack != null
        command != null
        command.commands.size() == 2
        command.commands[0].description.name == 'shathel-core-stack'
        command.commands[0].composeModel.parsedYml.networks['00shathel_network'] == null //tests if enricher does not apply to self
        command.commands[1].description.name == 'dummy'
        def preparedCompose = command.commands[1].composeModel.parsedYml
        preparedCompose.networks['00shathel_network'].external == true
        preparedCompose.services.dummy.networks == ['00shathel_network']
        preparedCompose.services.dummy.labels['org.shathel.stack.gav'] == 'test.group:dummy:2.0'
        preparedCompose.services.dummy.labels['sidekick'] == 'true'

        when:
        solution.run(command)

        then:
        def preparedCompose2 = new Yaml().load(new File(root, "local/enriched/dummy-2.0-shathel/stack/docker-compose.yml").text)
        preparedCompose2.networks['00shathel_network'].external == true
        preparedCompose2.services.dummy.networks == ['00shathel_network']
        new File(root, "local/enriched/shathel-core-stack-1.2.3-shathel/post-provision").text == "Done"
        new File(root, "local/enriched/shathel-core-stack-1.2.3-shathel/pre-provision").text == "Done"

        when:
        command = stack.createStartCommand(false,environment)

        then:
        command != null
        command.commands.size() == 1

        when:
        def stopCommand = stack.createStopCommand(true, true,environment)

        then:
        stopCommand != null
        stopCommand.commands*.type == [StackCommand.Type.STOP, StackCommand.Type.STOP]


        when:
        solution.run(stopCommand)

        then:
        stack.createStartCommand(false,environment).commands.size() == 2


        onEnd()

    }


}
