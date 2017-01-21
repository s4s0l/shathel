package org.s4s0l.shathel.commons.core

import org.apache.commons.io.FileUtils
import org.s4s0l.shathel.commons.DefaultExtensionContext
import org.s4s0l.shathel.commons.core.provision.StackCommand
import org.s4s0l.shathel.commons.core.stack.StackReference
import org.s4s0l.shathel.commons.utils.IoUtils
import org.yaml.snakeyaml.Yaml
import spock.lang.Specification

/**
 * @author Matcin Wielgus
 */
class StackTest extends Specification {


    def "Run stack in local docker integration test"() {
        given:
        File root = new File("build/localBasicRunTmp")
        def parameters = prepare(root, "sampleDependencies")
        SolutionFactory factory = new SolutionFactory(parameters, DefaultExtensionContext.create(parameters))

        when:
        def solution = factory.open(root, "DEV")

        then:
        solution != null;

        when:
        def stack = solution.openStack(new StackReference("test.group:dummy:2.0"))

        then:
        stack != null
        new File(root, "deps/dummy-2.0-shathel").isDirectory()
        new File(root, "deps/shathel-core-stack-1.2.3-shathel").isDirectory()

        when:
        def command = stack.createStartCommand();

        then:
        command != null
        command.commands.size() == 2
        command.commands[0].description.name == 'shathel-core-stack'
        command.commands[0].mutableModel.parsedYml.networks['00shathel_network'] == null //tests if enricher does not apply to self
        command.commands[1].description.name == 'dummy'
        def preparedCompose = command.commands[1].mutableModel.parsedYml
        preparedCompose.networks['00shathel_network'].external == true
        preparedCompose.services.dummy.networks == ['00shathel_network']
        preparedCompose.services.dummy.labels['org.shathel.stack.gav'] == 'test.group:dummy:2.0'

        when:
        stack.run(command)

        then:
        def preparedCompose2 = new Yaml().load(new File(root, "execution/dummy-2.0-shathel/stack/docker-compose.yml").text)
        preparedCompose2.networks['00shathel_network'].external == true
        preparedCompose2.services.dummy.networks == ['00shathel_network']


        when:
        command = stack.createStartCommand()

        then:
        command != null
        command.commands.isEmpty()

        when:
        def stopCommand = stack.createStopCommand(true)

        then:
        stopCommand != null
        stopCommand.commands*.type == [StackCommand.Type.STOP,StackCommand.Type.STOP]


        when:
        stack.run(stopCommand)

        then:
        stack.createStartCommand().commands.size() == 2


    }


    Parameters prepare(File root, String sourceDir) {
        File src = new File("src/test/$sourceDir")

        FileUtils.deleteDirectory(root)
        def deps = new File(root, "deps")
        deps.mkdirs()
        Parameters parameters = Parameters.builder()
                .parameter("storage.dependencies.dir", deps.absolutePath)
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
