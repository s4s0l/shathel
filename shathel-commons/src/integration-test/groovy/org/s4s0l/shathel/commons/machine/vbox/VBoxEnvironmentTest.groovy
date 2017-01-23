package org.s4s0l.shathel.commons.machine.vbox

import org.apache.commons.io.FileUtils
import org.s4s0l.shathel.commons.Shathel
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
class VBoxEnvironmentTest
        extends Specification {

    def setupSpec() {
        FileUtils.deleteDirectory(new File(getRootDir()));
        cleanOnEnd()
    }

    boolean cleanOnEnd() {
        new DockerComposeWrapper().with {
            removeAllForComposeProject("dummy")
            removeAllForComposeProject("00shathel")
        }
        true
    }

    def "Run stack in local docker integration test"() {
        given:
        File root = new File(getRootDir())
        def parameters = prepare(root, "sampleDependencies")
        Shathel sht = new Shathel(parameters)
        def solution = sht.getSolution(sht.initStorage(root))
        def environment = solution.getEnvironment("itg")

        when:
        def stack = solution.openStack(environment, new StackReference("test.group:dummy:2.0"))
        def command = stack.createStartCommand();

        then:
        command != null
        command.commands.size() == 2
        command.commands[0].description.name == 'shathel-core-stack'
        command.commands[1].description.name == 'dummy'

        when:
        stack.run(command)

        then:
        def preparedCompose2 = new Yaml().load(new File(root, "tmp/itg/execution/dummy-2.0-shathel/stack/docker-compose.yml").text)
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
        stopCommand.commands*.type == [StackCommand.Type.STOP, StackCommand.Type.STOP]

        when:
        stack.run(stopCommand)

        then:
        stack.createStartCommand().commands.size() == 2


        cleanOnEnd()
    }

    private GString getRootDir() {
        "build/Test${getClass().getSimpleName()}"
    }


    Parameters prepare(File root, String sourceDir) {
        File src = new File("src/test/$sourceDir")
        def deps = new File(root, "tmp/dependencies")
        deps.mkdirs()
        Parameters parameters = Parameters.builder()
                .parameter("shathel.safe.itg.password", "MySecretPassword")
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


