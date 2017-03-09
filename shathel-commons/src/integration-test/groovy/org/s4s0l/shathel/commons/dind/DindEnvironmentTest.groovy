package org.s4s0l.shathel.commons.dind

import org.apache.commons.io.FileUtils
import org.s4s0l.shathel.commons.Shathel
import org.s4s0l.shathel.commons.core.MapParameters
import org.s4s0l.shathel.commons.core.Parameters
import org.s4s0l.shathel.commons.docker.DockerWrapper
import org.s4s0l.shathel.commons.utils.IoUtils
import spock.lang.Specification
import testutils.BaseIntegrationTest

/**
 * @author Matcin Wielgus
 */
class DindEnvironmentTest extends BaseIntegrationTest {
    @Override
    def setupEnvironment() {
        environmentName = "dind"
        network = "223.223.223"
    }

    def cleanupEnvironment() {
        new DockerWrapper().with {
            containerRemoveIfPresent("DindEnvironmentTest-dev-manager-1")
            containerRemoveIfPresent("DindEnvironmentTest-dev-manager-2")
            containerRemoveIfPresent("DindEnvironmentTest-dev-worker-1")
            if (networkExistsByFilter("name=shathel-dindenvironmenttest-dev"))
                networkRemove("shathel-dindenvironmenttest-dev")
        }
        true
    }

    def "Run stack in local docker integration test"() {
        given:
        File root = getRootDir()
        def parameters = prepare()
        Shathel sht = new Shathel(parameters)

        when:
        def storage = sht.initStorage(root, true)

        then:
        new File(root, "shathel-solution.yml").text.contains("name: ${getClass().getSimpleName()}")


        when:
        def solution = sht.getSolution(storage)
        def environment = solution.getEnvironment("dev")

        then:
        environment != null;

        when:
        if (!environment.isInitialized()) {
            environment.initialize()
        }
        if (!environment.isStarted()) {
            environment.start()
        }
        then:
        new DockerWrapper().containerRunning("DindEnvironmentTest-dev-manager-1")
        new DockerWrapper().containerRunning("DindEnvironmentTest-dev-manager-2")
        new DockerWrapper().containerRunning("DindEnvironmentTest-dev-worker-1")
//        new DockerWrapper(new ExecWrapper("docker --host " + new DockerWrapper().containerGetIpInNetwork("DindTest-dev-manager-1", "shathel-dindtest-dev"))).swarmActive()
//        new DockerWrapper(new ExecWrapper("docker --host " + new DockerWrapper().containerGetIpInNetwork("DindTest-dev-manager-2", "shathel-dindtest-dev"))).swarmActive()
//        new DockerWrapper(new ExecWrapper("docker --host " + new DockerWrapper().containerGetIpInNetwork("DindTest-dev-worker-1", "shathel-dindtest-dev"))).swarmActive()
//
//        def stack = solution.openStack(environment, new StackReference("test.group:dummy:2.0"))
//
//        then:
//        stack != null
//        new File(root, "deps/dummy-2.0-shathel").isDirectory()
//        new File(root, "deps/shathel-core-stack-1.2.3-shathel").isDirectory()
//
//        when:
//        def command = stack.createStartCommand();
//
//        then:
//        command != null
//        command.commands.size() == 2
//        command.commands[0].description.name == 'shathel-core-stack'
//        command.commands[0].mutableModel.parsedYml.networks['00shathel_network'] == null //tests if enricher does not apply to self
//        command.commands[1].description.name == 'dummy'
//        def preparedCompose = command.commands[1].mutableModel.parsedYml
//        preparedCompose.networks['00shathel_network'].external == true
//        preparedCompose.services.dummy.networks == ['00shathel_network']
//        preparedCompose.services.dummy.labels['org.shathel.stack.gav'] == 'test.group:dummy:2.0'
//
//        when:
//        stack.run(command)
//
//        then:
//        def preparedCompose2 = new Yaml().load(new File(root, "tmp/composed/execution/dummy-2.0-shathel/stack/docker-compose.yml").text)
//        preparedCompose2.networks['00shathel_network'].external == true
//        preparedCompose2.services.dummy.networks == ['00shathel_network']
//
//
//        when:
//        command = stack.createStartCommand()
//
//        then:
//        command != null
//        command.commands.isEmpty()
//
//        when:
//        def stopCommand = stack.createStopCommand(true)
//
//        then:
//        stopCommand != null
//        stopCommand.commands*.type == [StackCommand.Type.STOP, StackCommand.Type.STOP]
//
//
//        when:
//        stack.run(stopCommand)
//
//        then:
//        stack.createStartCommand().commands.size() == 2

        onEnd()

    }


}
