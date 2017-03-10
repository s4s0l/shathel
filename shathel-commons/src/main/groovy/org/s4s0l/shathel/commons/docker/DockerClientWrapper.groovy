package org.s4s0l.shathel.commons.docker

import de.gesellix.docker.client.DockerClientImpl
import de.gesellix.docker.client.config.DockerEnv

/**
 * @author Marcin Wielgus
 */
class DockerClientWrapper {

    @Delegate
    DockerClientImpl dockerClient

    DockerClientWrapper(Map<String, String> dockerEnvs) {
        if (dockerEnvs['DOCKER_HOST'] == null)
            dockerClient = new DockerClientImpl()
        else {
            DockerEnv env = new DockerEnv(
                    dockerHost: dockerEnvs.DOCKER_HOST,
                    tlsVerify: dockerEnvs.DOCKER_TLS_VERIFY,
                    certPath: dockerEnvs.DOCKER_CERT_PATH
            )
            dockerClient = new DockerClientImpl(env)
        }

    }
}
