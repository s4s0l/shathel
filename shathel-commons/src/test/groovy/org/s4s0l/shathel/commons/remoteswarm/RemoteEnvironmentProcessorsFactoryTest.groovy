package org.s4s0l.shathel.commons.remoteswarm

import org.mockito.Mockito
import org.s4s0l.shathel.commons.DefaultExtensionContext
import org.s4s0l.shathel.commons.core.Parameters
import spock.lang.Specification

/**
 * @author Marcin Wielgus
 */
class RemoteEnvironmentProcessorsFactoryTest extends Specification {


    def "using default extension context ProcessorsFactory should be able to produce runnable ansible scripts"() {
        given:
        def api = Mockito.mock(RemoteEnvironmentApiFacade)
        Mockito.when(api.getNodes()).thenReturn([])

        def context = Mockito.mock(RemoteEnvironmentPackageContext)
        Mockito.when(context.getRemoteUser()).thenReturn("root")
        Mockito.when(context.getKeysDirectory()).thenReturn(scriptRoot)
        Mockito.when(context.getAnsibleInventoryFile()).thenReturn(new File(scriptRoot, "ai"))

        RemoteEnvironmentProcessorsFactory rep = new RemoteEnvironmentProcessorsFactory(api, DefaultExtensionContext.create(Parameters.fromMapWithSysPropAndEnv([:])), context)

        when:
        def processor = rep.create( new RemoteEnvironmentScript("ansible", "empty-playbook.yml", "gav", getScriptRoot()))
        def process = processor.process(ProcessorCommand.APPLY, [TARGET_DIR: targetDir.absolutePath])

        then:
        [TARGET_DIR: targetDir.absolutePath, ANSIBLE_HOST_KEY_CHECKING: "False", ANSIBLE_NOCOWS: "1"] == process
        new File(targetDir, "out.txt").text == "[\"192.168.99.4\"]"
    }

    def "using default extension context should be able to produce vagrant scripts"() {
        given:
        def api = Mockito.mock(RemoteEnvironmentApiFacade)
        Mockito.when(api.getNodes()).thenReturn([])

        def context = Mockito.mock(RemoteEnvironmentPackageContext)
        Mockito.when(context.getSettingsDirectory()).thenReturn(targetDir)

        RemoteEnvironmentProcessorsFactory rep = new RemoteEnvironmentProcessorsFactory(api, DefaultExtensionContext.create(Parameters.fromMapWithSysPropAndEnv([:])), context)

        def envs = [DOCKER_NAME: "${getClass().simpleName}"]
        def expectedMap = [DOCKER_NAME: "${getClass().simpleName}", VAGRANT_VAGRANTFILE: "Vagrantfile", VAGRANT_DOTFILE_PATH: "${targetDir.absolutePath}"]
        when:
        def processor = rep.create( new RemoteEnvironmentScript("vagrant", "Vagrantfile", "gav", getScriptRoot()))
        def process = processor.process(ProcessorCommand.INITED, envs)

        then:
        ["RESULT": "false"] + expectedMap == process

        when:
        process = processor.process(ProcessorCommand.STARTED, envs)

        then:
        ["RESULT": "false"] + expectedMap == process

        when:
        process = processor.process(ProcessorCommand.APPLY, envs)

        then:
        expectedMap == process

        when:
        process = processor.process(ProcessorCommand.INITED, envs)

        then:
        ["RESULT": "true"] + expectedMap == process

        when:
        process = processor.process(ProcessorCommand.STARTED, envs)

        then:
        ["RESULT": "true"] + expectedMap == process

        when:
        process = processor.process(ProcessorCommand.STOP, envs)

        then:
        expectedMap == process

        when:
        process = processor.process(ProcessorCommand.INITED, envs)

        then:
        ["RESULT": "true"] + expectedMap == process

        when:
        process = processor.process(ProcessorCommand.STARTED, envs)

        then:
        ["RESULT": "false"] + expectedMap == process

        when:
        process = processor.process(ProcessorCommand.DESTROY, envs)

        then:
        expectedMap == process


    }

    def "using default extension context should be able to produce terraform scripts"() {

    }

    def "using default extension context should be able to produce groovy scripts"() {
        given:
        def api = Mockito.mock(RemoteEnvironmentApiFacade)
        Mockito.when(api.getNodes()).thenReturn([])

        def context = Mockito.mock(RemoteEnvironmentPackageContext)
        Mockito.when(context.getSettingsDirectory()).thenReturn(targetDir)
        Mockito.when(context.getRemoteUser()).thenReturn("root")
        Mockito.when(context.getKeysDirectory()).thenReturn(scriptRoot)
        Mockito.when(context.getAnsibleInventoryFile()).thenReturn(new File(scriptRoot, "ai"))

        RemoteEnvironmentProcessorsFactory rep = new RemoteEnvironmentProcessorsFactory(api, DefaultExtensionContext.create(Parameters.fromMapWithSysPropAndEnv([:])), context)

        when:
        def processor = rep.create( new RemoteEnvironmentScript("groovy", "script.groovy", "gav", getScriptRoot()))
        def process = processor.process(ProcessorCommand.APPLY, [SAMPLE_ENV: 'value'])

        then:
        [SAMPLE_ENV: 'value'] == process
        new File(targetDir, "groovy_out.txt").text == "hello"
    }

    def "groovy scripts should be able to run callbacks"() {
        given:
        def api = Mockito.mock(RemoteEnvironmentApiFacade)
        Mockito.when(api.getNodes()).thenReturn([])

        def context = Mockito.mock(RemoteEnvironmentPackageContext)
        Mockito.when(context.getSettingsDirectory()).thenReturn(targetDir)
        Mockito.when(context.getRemoteUser()).thenReturn("root")
        Mockito.when(context.getKeysDirectory()).thenReturn(scriptRoot)
        Mockito.when(context.getPackageRootDirectory()).thenReturn(scriptRoot)
        Mockito.when(context.getGav()).thenReturn("gav")
        Mockito.when(context.getAnsibleInventoryFile()).thenReturn(new File(scriptRoot, "ai"))

        RemoteEnvironmentProcessorsFactory rep = new RemoteEnvironmentProcessorsFactory(api, DefaultExtensionContext.create(Parameters.fromMapWithSysPropAndEnv([:])), context)

        when:
        def processor = rep.create( new RemoteEnvironmentScript("groovy", "groovy-callback.groovy", "gav", getScriptRoot()))
        def process = processor.process(ProcessorCommand.APPLY, [TARGET_DIR: targetDir.absolutePath])

        then:
        [TARGET_DIR: targetDir.absolutePath] == process
        new File(targetDir, "groovy_callback_out.txt").text == "hello"
        new File(targetDir, "groovy_callback_ansible_out.txt").text == "hello"
    }


    private File getTargetDir() {
        new File("./build/${getClass().getSimpleName()}")
    }

    private File getScriptRoot() {
        new File("src/test/resources/scripts")
    }
}
