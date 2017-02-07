package org.s4s0l.shathel.commons.dind

import org.apache.commons.io.FileUtils
import org.s4s0l.shathel.commons.Shathel
import org.s4s0l.shathel.commons.core.MapParameters
import org.s4s0l.shathel.commons.core.Parameters
import org.s4s0l.shathel.commons.core.provision.StackCommand
import org.s4s0l.shathel.commons.core.stack.StackReference
import org.s4s0l.shathel.commons.docker.DockerComposeWrapper
import org.s4s0l.shathel.commons.docker.DockerWrapper
import org.s4s0l.shathel.commons.utils.ExecWrapper
import org.s4s0l.shathel.commons.utils.IoUtils
import org.yaml.snakeyaml.Yaml
import spock.lang.Specification

/**
 * @author Matcin Wielgus
 */
class DindEnvironmentTest extends Specification {

    def setupSpec() {
        cleanupSpecX();
        FileUtils.deleteDirectory(new File(getRootDir()));

    }

    def cleanupSpecX() {
        new DockerWrapper().with {
            containerRemoveIfPresent("DindTest-dev-manager-1")
            containerRemoveIfPresent("DindTest-dev-manager-2")
            containerRemoveIfPresent("DindTest-dev-worker-1")
            if(networkExistsByFilter("name=shathel-dindtest-dev"))
            networkRemove("shathel-dindtest-dev")
        }
        true
    }

    def "Run stack in local docker integration test"() {
        given:
        File root = new File(getRootDir())
        def parameters = prepare(root, "sampleDependencies")
        Shathel sht = new Shathel(parameters)

        when:
        def storage = sht.initStorage(root, true)

        then:
        new File(root, "shathel-solution.yml").text.contains("name: DindTest")


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
        new DockerWrapper().containerRunning("DindTest-dev-manager-1")
        new DockerWrapper().containerRunning("DindTest-dev-manager-2")
        new DockerWrapper().containerRunning("DindTest-dev-worker-1")
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

        cleanupSpecX()

    }

    private GString getRootDir() {
        "build/Test${getClass().getSimpleName()}"
    }


    Parameters prepare(File root, String sourceDir) {
        File src = new File("src/test/$sourceDir")
        def deps = new File(root, "deps")
        deps.mkdirs()
        Parameters parameters = MapParameters.builder()
                .parameter("shathel.storage.tmp.dependencies.dir", deps.absolutePath)
                .parameter("shathel.solution.name", "DindTest")
                .parameter("shathel.env.dev.net", "22.22.22")
                .build()
        src.listFiles()
                .findAll { it.isDirectory() }
                .each {
            IoUtils.zipIt(it,
                    new File(deps, "${it.getName()}.zip"))
        }
        return parameters;
    }
}
