package org.s4s0l.shathel.commons.machine.vbox

import org.apache.commons.io.FileUtils
import org.s4s0l.shathel.commons.Shathel
import org.s4s0l.shathel.commons.core.Parameters
import org.s4s0l.shathel.commons.core.provision.StackCommand
import org.s4s0l.shathel.commons.core.stack.StackReference
import org.s4s0l.shathel.commons.docker.DockerMachineWrapper
import org.s4s0l.shathel.commons.utils.IoUtils
import spock.lang.Specification

/**
 * @author Matcin Wielgus
 */
class VBoxEnvironmentTest
        extends Specification {

    def setupSpec() {
        cleanOnEnd()
        FileUtils.deleteDirectory(new File(getRootDir()));
    }

    boolean cleanOnEnd() {
        def file = new File(getRootDir(), "tmp/itg/settings")
        if (file.exists()) {
            def wrapper = new DockerMachineWrapper(file)
            new File(file, "machines").listFiles().each {
                wrapper.remove(it.name)
            }
        }

        true
    }

    def "Run stack in vbox integration test"() {
        given:
        File root = new File(getRootDir())
        def parameters = prepare(root, "sampleDependencies")
        Shathel sht = new Shathel(parameters)
        def solution = sht.getSolution(sht.initStorage(root,false))
        def environment = solution.getEnvironment("itg")

        when:
        if (!environment.isInitialized()) {
            environment.initialize()
        }
        if (!environment.isStarted()) {
            environment.start()
        }
        def stack = solution.openStack(environment, new StackReference("test.group:dummy:2.0"))
        def command = stack.createStartCommand();

        then:
        command.commands.size() == 2

        when:
        stack.run(command)

        then:
        stack.createStartCommand().commands.isEmpty()

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

    private String getRootDir() {
        "build/Test${getClass().getSimpleName()}"
    }

    private String getClusterName() {
        return getClass().getSimpleName()
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
                .findAll { !new File(deps, "${it.getName()}.zip").exists() }
                .each {
            IoUtils.zipIt(it,
                    new File(deps, "${it.getName()}.zip"))
        }
        return parameters;
    }

}


