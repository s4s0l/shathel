package org.s4s0l.shathel.commons.machine.vbox

import org.apache.commons.io.FileUtils
import org.s4s0l.shathel.commons.Shathel
import org.s4s0l.shathel.commons.core.MapParameters
import org.s4s0l.shathel.commons.core.Parameters
import org.s4s0l.shathel.commons.core.environment.StackCommand
import org.s4s0l.shathel.commons.core.stack.StackReference
import org.s4s0l.shathel.commons.docker.DockerMachineWrapper
import org.s4s0l.shathel.commons.utils.IoUtils
import spock.lang.Specification
import testutils.BaseIntegrationTest

/**
 * @author Matcin Wielgus
 */
class VBoxEnvironmentTest
        extends BaseIntegrationTest {

    @Override
    def setupEnvironment() {
        environmentName = "itg"
        network = "214.214.214"
    }

    def cleanupEnvironment() {
        def file = new File(getRootDir(), "itg/settings")
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
        File root = getRootDir()
        def parameters = prepare()
        Shathel sht = new Shathel(parameters)
        def solution = sht.getSolution(sht.initStorage(root, false))
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
        stack.createStartCommand().commands.size() == 1

        when:
        def stopCommand = stack.createStopCommand(true)

        then:
        stopCommand.commands*.type == [StackCommand.Type.STOP, StackCommand.Type.STOP]

        when:
        stack.run(stopCommand)

        then:
        stack.createStartCommand().commands.size() == 2

        onEnd()
    }


}


