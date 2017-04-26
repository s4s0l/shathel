package org.s4s0l.shathel.commons.remoteswarm

import org.mockito.Mockito
import org.s4s0l.shathel.commons.DefaultExtensionContext
import org.s4s0l.shathel.commons.core.CommonParams
import org.s4s0l.shathel.commons.core.Parameters
import org.s4s0l.shathel.commons.utils.ExtensionContext
import spock.lang.Specification

/**
 * @author Marcin Wielgus
 */
class RemoteEnvironmentProcessorsFactoryTest extends Specification {


    def "Ansible scripts should be runnable"() {
        given:
        def api = Mockito.mock(RemoteEnvironmentApiFacade)
        Mockito.when(api.getNodes()).thenReturn([])

        def context = Mockito.mock(RemoteEnvironmentPackageContext)
        Mockito.when(context.getRemoteUser()).thenReturn("root")
        Mockito.when(context.getKeysDirectory()).thenReturn(scriptRoot)
        Mockito.when(context.getTempDirectory()).thenReturn(targetDir)
        Mockito.when(context.getAnsibleInventoryFile()).thenReturn(new File(scriptRoot, "ai"))

        RemoteEnvironmentProcessorsFactory rep = new RemoteEnvironmentProcessorsFactory(api, extensionContext, context)
        def envs = [TARGET_DIR: targetDir.absolutePath]

        when:
        def processor = rep.create(new RemoteEnvironmentScript("ansible", "empty-playbook.yml", "gav", getScriptRoot()))
        def process = processor.process(ProcessorCommand.APPLY, envs)

        then:
        [TARGET_DIR: targetDir.absolutePath, ANSIBLE_RETRY_FILES_SAVE_PATH: targetDir.absolutePath, ANSIBLE_HOST_KEY_CHECKING: "False", ANSIBLE_NOCOWS: "1"] == envs
        process.status
        process.output.contains("127.0.0.1")
        new File(targetDir, "out.txt").text == "[\"192.168.92.4\"]"
    }

    def "Vagrant scripts should be runnable"() {
        given:
        def api = Mockito.mock(RemoteEnvironmentApiFacade)
        Mockito.when(api.getNodes()).thenReturn([])

        def context = Mockito.mock(RemoteEnvironmentPackageContext)
        Mockito.when(context.getRemoteUser()).thenReturn("root")
        Mockito.when(context.getSettingsDirectory()).thenReturn(targetDir)
        Mockito.when(context.getDependencyCacheDirectory()).thenReturn(targetDir)
        Mockito.when(context.getAnsibleInventoryFile()).thenReturn(targetDir)

        RemoteEnvironmentProcessorsFactory rep = new RemoteEnvironmentProcessorsFactory(api, extensionContext, context)

        def envs = [DOCKER_NAME: "${getClass().simpleName}"]
        def expectedMap = [
                DOCKER_NAME: "${getClass().simpleName}",
                VAGRANT_HOME: new File(targetDir, ".vagrant.d").absolutePath,
                VAGRANT_VAGRANTFILE: "Vagrantfile",
                VAGRANT_DOTFILE_PATH: "${targetDir.absolutePath}"]
        when:
        def processor = rep.create(new RemoteEnvironmentScript("vagrant", "Vagrantfile", "gav", getScriptRoot()))
        def process = processor.process(ProcessorCommand.INITED, envs)
        then:
        expectedMap == envs
        !process.status

        when:
        process = processor.process(ProcessorCommand.STARTED, envs)
        then:
        !process.status

        when:
        process = processor.process(ProcessorCommand.APPLY, envs)
        then:
        expectedMap == envs
        process.status


        when:
        process = processor.process(ProcessorCommand.INITED, envs)
        then:
        process.status


        when:
        process = processor.process(ProcessorCommand.STARTED, envs)
        then:
        process.status


        when:
        process = processor.process(ProcessorCommand.STOP, envs)
        then:
        expectedMap == envs
        process.status


        when:
        process = processor.process(ProcessorCommand.INITED, envs)
        then:
        process.status


        when:
        process = processor.process(ProcessorCommand.STARTED, envs)
        then:
        !process.status

        when:
        process = processor.process(ProcessorCommand.DESTROY, envs)
        then:
        expectedMap == envs
        process.status
    }

    def "Terraform scripts should be runnable"() {
        given:
        def api = Mockito.mock(RemoteEnvironmentApiFacade)
        Mockito.when(api.getNodes()).thenReturn([])

        def context = Mockito.mock(RemoteEnvironmentPackageContext)
        Mockito.when(context.getRemoteUser()).thenReturn("root")
        Mockito.when(context.getSettingsDirectory()).thenReturn(targetDir)
        Mockito.when(context.getTempDirectory()).thenReturn(targetDir)
        Mockito.when(context.getAnsibleInventoryFile()).thenReturn(targetDir)

        RemoteEnvironmentProcessorsFactory rep = new RemoteEnvironmentProcessorsFactory(api, extensionContext, context)

        def envs = ['container_name': 'terraform_test']

        when:
        def processor = rep.create(new RemoteEnvironmentScript("terraform", "./tf", "gav", getScriptRoot()))
        then:
        processor.process(ProcessorCommand.INITED, envs).status == false
        processor.process(ProcessorCommand.STARTED, envs).status == false

        when:
        def process = processor.process(ProcessorCommand.APPLY, envs)

        then:
        process.status
        envs['SOME_OUTPUT'] == "My value"
        processor.process(ProcessorCommand.INITED, envs).status == true
        processor.process(ProcessorCommand.STARTED, envs).status == true
        processor.process(ProcessorCommand.STOP, envs).status == true
        //because terraform cannot tell if is started or not
        processor.process(ProcessorCommand.INITED, envs).status == true
        processor.process(ProcessorCommand.STARTED, envs).status == true
        processor.process(ProcessorCommand.DESTROY, envs).status == true
        processor.process(ProcessorCommand.INITED, envs).status == false
        processor.process(ProcessorCommand.STARTED, envs).status == false

    }

    def "Groovy scripts should be runnable"() {
        given:
        def api = Mockito.mock(RemoteEnvironmentApiFacade)
        Mockito.when(api.getNodes()).thenReturn([])

        def context = Mockito.mock(RemoteEnvironmentPackageContext)
        Mockito.when(context.getSettingsDirectory()).thenReturn(targetDir)
        Mockito.when(context.getRemoteUser()).thenReturn("root")
        Mockito.when(context.getKeysDirectory()).thenReturn(scriptRoot)
        Mockito.when(context.getAnsibleInventoryFile()).thenReturn(new File(scriptRoot, "ai"))

        RemoteEnvironmentProcessorsFactory rep = new RemoteEnvironmentProcessorsFactory(api, extensionContext, context)
        def envs = [SAMPLE_ENV: 'value']
        when:
        def processor = rep.create(new RemoteEnvironmentScript("groovy", "script.groovy", "gav", getScriptRoot()))
        def process = processor.process(ProcessorCommand.APPLY, envs)

        then:
        process.status == false
        process.output == "OUTPUT"
        envs == [SAMPLE_ENV: 'value', RETURNED_ENV: 'value']
        new File(targetDir, "groovy_out.txt").text == "hello"
    }


    def "Groovy scripts should be able to call other processors (ansible, terraform, vagrant) via callbacks"() {
        given:
        def api = Mockito.mock(RemoteEnvironmentApiFacade)
        Mockito.when(api.getNodes()).thenReturn([])

        def context = Mockito.mock(RemoteEnvironmentPackageContext)
        Mockito.when(context.getSettingsDirectory()).thenReturn(targetDir)
        Mockito.when(context.getTempDirectory()).thenReturn(targetDir)
        Mockito.when(context.getDependencyCacheDirectory()).thenReturn(targetDir)
        Mockito.when(context.getRemoteUser()).thenReturn("root")
        Mockito.when(context.getKeysDirectory()).thenReturn(scriptRoot)
        Mockito.when(context.getPackageRootDirectory()).thenReturn(scriptRoot)
        Mockito.when(context.getGav()).thenReturn("gav")
        Mockito.when(context.getAnsibleInventoryFile()).thenReturn(new File(scriptRoot, "ai"))

        RemoteEnvironmentProcessorsFactory rep = new RemoteEnvironmentProcessorsFactory(api, extensionContext, context)

        when:
        def processor = rep.create(new RemoteEnvironmentScript("groovy", "groovy-callback.groovy", "gav", getScriptRoot()))
        def process = processor.process(ProcessorCommand.APPLY, [TARGET_DIR           : targetDir.absolutePath,
                                                                 'container_name'     : 'groovy_callback_ansible',
                                                                 'PACKER_ENV_VARIABLE': 'ubuntu'])

        then:
        process.status
        new File(targetDir, "groovy_callback_out.txt").text == "hello"
        new File(targetDir, "groovy_callback_ansible_out.txt").text == "hello"
    }


    private File getTargetDir() {
        new File("./build/${getClass().getSimpleName()}")
    }

    private File getScriptRoot() {
        new File("src/test/resources/scripts")
    }

    private ExtensionContext getExtensionContext() {
        DefaultExtensionContext.create(Parameters.fromMapWithSysPropAndEnv([(CommonParams.SHATHEL_DIR): getTargetDir().absolutePath]))
    }
}
