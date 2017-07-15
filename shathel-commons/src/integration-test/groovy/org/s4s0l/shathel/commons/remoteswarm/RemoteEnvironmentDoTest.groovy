package org.s4s0l.shathel.commons.remoteswarm

import org.s4s0l.shathel.commons.Shathel
import org.s4s0l.shathel.commons.core.stack.StackReference
import org.s4s0l.shathel.commons.docker.VBoxManageWrapper
import org.s4s0l.shathel.commons.utils.IoUtils
import testutils.BaseIntegrationTest

/**
 * @author Marcin Wielgus
 */

class RemoteEnvironmentDoTest extends BaseIntegrationTest {
    @Override
    def setupEnvironment() {
        environmentName = "do"
        solutionDescription = """
version: 1
shathel-solution:
  name: ${getClass().getSimpleName()}
  docker_version: 17.06.0
  environments:
    do:
      forceful: 'true'
      pull: true
      type: remote
      gav: ./digital-ocean
      build-allowed: true
      managers: 2
      workers: 1
"""
    }

    def cleanupEnvironment() {
        if (rootDir.exists()) {
            Shathel sht = shathel([
                    "shathel.solution.file_env_base_dir": "../../shathel-envs"
            ] << privateProperties)
            def solution = sht.getSolution(sht.getStorage(getRootDir()))
            def environment = solution.getEnvironment(environmentName)
            if (environment.isInitialized()) {
                environment.destroy()
            }
        }
    }

    def "Remote envirnment should basically work"() {
        given:
        Shathel sht = shathel([
                "shathel.solution.file_env_base_dir": "../../shathel-envs"
        ] << privateProperties)

        when:
        def solution = sht.getSolution(sht.getStorage(getRootDir()))
        def environment = solution.getEnvironment(environmentName)

        then:
        !environment.isInitialized()
        !environment.isStarted()

        when:
        environment.initialize()

        then:
        environment.isInitialized()
        environment.isStarted()
        environment.getEnvironmentApiFacade().nodes.size() == 3
        environment.getEnvironmentApiFacade().nodes.findAll {
            it.role == "manager"
        }.size() == 2
        environment.getEnvironmentApiFacade().nodes.findAll {
            it.role == "worker"
        }.size() == 1
        environment.getEnvironmentApiFacade().secretManager != null
        environment.getEnvironmentApiFacade().getDockerEnvs(environment.getEnvironmentApiFacade().managerNode)['DOCKER_HOST'].contains("127.0.0.1")
        environment.getEnvironmentApiFacade().getManagerNodeWrapper().swarmNodes().size() == 3

        //firewalls should not allow to connect via public ip to docker
        when:
        IoUtils.waitForSocket(environment.getEnvironmentApiFacade().getManagerNode().publicIp, 2376, 3, new RuntimeException())
        then:
        thrown(RuntimeException)


        when:
        def stack = solution.openStack(environment, new StackReference("test.group:dummy:2.0"))
        def command = stack.createStartCommand(false)

        then:
        command.commands.size() == 2

        when:
        solution.run(command)

        then:
        environment.getIntrospectionProvider().allStacks.size() == 2

        when:
        environment.stop()

        then:
        environment.isInitialized()
        environment.isStarted()

        when:
        environment.start()

        then:
        environment.isInitialized()
        environment.isStarted()
        environment.getIntrospectionProvider().allStacks.size() == 2

        when:
        environment.destroy()

        then:
        !environment.isInitialized()
        !environment.isStarted()

        onEnd()

    }


}
