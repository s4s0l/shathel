package org.s4s0l.shathel.commons.docker

/**
 * @author Marcin Wielgus
 */
class DockerMachineNode {
    final String name
    final String driver
    final String state
    final String url
    final String version
    final Map<String, String> envs
    final String ip
    final DockerInfoWrapper swarmInfo
    final DockerWrapper docker;

    DockerMachineNode(String name, String driver, String state, String url, String version,
                      Map<String, String> envs, String ip, DockerInfoWrapper swarmInfo, DockerWrapper docker) {
        this.name = name
        this.driver = driver
        this.state = state
        this.url = url
        this.version = version
        this.envs = envs
        this.ip = ip
        this.swarmInfo = swarmInfo
        this.docker = docker
    }

    boolean isRunning() {
        return "Running" == state
    }

}
