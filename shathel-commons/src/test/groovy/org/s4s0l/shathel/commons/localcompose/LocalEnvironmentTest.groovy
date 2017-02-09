package org.s4s0l.shathel.commons.localcompose

import org.apache.commons.io.FileUtils
import org.s4s0l.shathel.commons.Shathel
import org.s4s0l.shathel.commons.core.MapParameters
import org.s4s0l.shathel.commons.core.Parameters
import org.s4s0l.shathel.commons.core.provision.StackCommand
import org.s4s0l.shathel.commons.core.stack.StackReference
import org.s4s0l.shathel.commons.docker.DockerComposeWrapper
import org.s4s0l.shathel.commons.utils.IoUtils
import org.yaml.snakeyaml.Yaml
import spock.lang.Specification

/**
 * @author Matcin Wielgus
 */
class LocalEnvironmentTest extends Specification {

    def setupSpec() {
        FileUtils.deleteDirectory(new File(getRootDir()));
        cleanupSpec();
    }

    def cleanupSpec() {
        new DockerComposeWrapper().with {
            removeAllForComposeProject("dummy")
            removeAllForComposeProject("00shathel")
        }
    }

    def "Run stack in local docker integration test"() {
        given:
        File root = new File(getRootDir())
        def parameters = prepare(root, "sampleDependencies")
        Shathel sht = new Shathel(parameters)

        when:
        def storage = sht.initStorage(root, true)

        then:
        new File(root, "shathel-solution.yml").text.contains("name: LocalComposeTest")


        when:
        def solution = sht.getSolution(storage)
        def environment = solution.getEnvironment("composed")

        then:
        environment != null;

        when:
        def stack = solution.openStack(environment, new StackReference("test.group:dummy:2.0"),false)

        then:
        stack != null
        new File(root, "deps/dummy-2.0-shathel").isDirectory()
        new File(root, "deps/shathel-core-stack-1.2.3-shathel").isDirectory()

        when:
        def command = stack.createStartCommand(false);

        then:
        command != null
        command.commands.size() == 2
        command.commands[0].description.name == 'shathel-core-stack'
        command.commands[0].composeModel.parsedYml.networks['00shathel_network'] == null //tests if enricher does not apply to self
        command.commands[1].description.name == 'dummy'
        def preparedCompose = command.commands[1].composeModel.parsedYml
        preparedCompose.networks['00shathel_network'].external == true
        preparedCompose.services.dummy.networks == ['00shathel_network']
        preparedCompose.services.dummy.labels['org.shathel.stack.gav'] == 'test.group:dummy:2.0'

        when:
        stack.run(command)

        then:
        def preparedCompose2 = new Yaml().load(new File(root, "tmp/composed/execution/dummy-2.0-shathel/stack/docker-compose.yml").text)
        preparedCompose2.networks['00shathel_network'].external == true
        preparedCompose2.services.dummy.networks == ['00shathel_network']
        new File(root, "tmp/composed/execution/shathel-core-stack-1.2.3-shathel/post-provision").text == "Done"
        new File(root, "tmp/composed/execution/shathel-core-stack-1.2.3-shathel/pre-provision").text == "Done"

        when:
        command = stack.createStartCommand(false)

        then:
        command != null
        command.commands.isEmpty()
        stack.createStartCommand(true).commands.size() == 2

        when:
        def stopCommand = stack.createStopCommand(true)

        then:
        stopCommand != null
        stopCommand.commands*.type == [StackCommand.Type.STOP, StackCommand.Type.STOP]


        when:
        stack.run(stopCommand)

        then:
        stack.createStartCommand(false).commands.size() == 2


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
                .parameter("shathel.solution.name", "LocalComposeTest")
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
