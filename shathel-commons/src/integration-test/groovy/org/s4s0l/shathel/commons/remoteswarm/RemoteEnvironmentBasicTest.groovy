package org.s4s0l.shathel.commons.remoteswarm

import org.s4s0l.shathel.commons.Shathel
import org.s4s0l.shathel.commons.core.stack.StackReference
import org.s4s0l.shathel.commons.docker.DockerWrapper
import org.s4s0l.shathel.commons.docker.VBoxManageWrapper
import testutils.BaseIntegrationTest

/**
 * @author Marcin Wielgus
 */

class RemoteEnvironmentBasicTest extends BaseIntegrationTest {
    @Override
    def setupEnvironment() {
        environmentName = "vbox"
        solutionDescription = """
version: 1
shathel-solution:
  name: ${getClass().getSimpleName()}
  environments:
    vbox:
      forceful: 'true'
      pull: true
      type: remote
      gav: ./virtualbox
      build-allowed: true
      managers: 2
      workers: 1
      domain: some.test.io
"""
    }

    def cleanupEnvironment() {
        new VBoxManageWrapper().with {
            poweroff("${getClass().getSimpleName()}-vbox-manager-1")
            removeVm("${getClass().getSimpleName()}-vbox-manager-1", true)
            poweroff("${getClass().getSimpleName()}-vbox-manager-2")
            removeVm("${getClass().getSimpleName()}-vbox-manager-2", true)
            poweroff("${getClass().getSimpleName()}-vbox-worker-1")
            removeVm("${getClass().getSimpleName()}-vbox-worker-1", true)
        }
        true
    }

    def "Remote envirnment should basically work"() {
        given:
        Shathel sht = shathel([
                "shathel.solution.file_env_base_dir": "../../shathel-envs"
        ])

        when:
        def solution = sht.getSolution(sht.getStorage(getRootDir()))
        def environment = solution.getEnvironment(environmentName)

        then:
        !environment.isInitialized()
        !environment.isStarted()


        onEnd()

    }


}
