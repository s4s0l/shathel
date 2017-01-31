package org.s4s0l.shathel.commons.docker

import org.s4s0l.shathel.commons.utils.ExecWrapper
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * @author Matcin Wielgus
 */
class DockerComposeWrapper {
    private static
    final Logger LOGGER = LoggerFactory.getLogger(DockerComposeWrapper.class);

    final ExecWrapper exec = new ExecWrapper(LOGGER, 'docker-compose')

    boolean up(File project, String projectName) {
        LOGGER.info("compose: starting project $projectName from ${project.absolutePath}")
        exec.executeForExitValue(project, "-p $projectName up -d") == 0
    }

    boolean down(File project, String projectName) {
        LOGGER.info("compose: stopping project $projectName from ${project.absolutePath}")
        exec.executeForExitValue(project, "-p $projectName down --remove-orphans") == 0
    }
    /**
     * removes all containers,networks for given docker-compose project name
     * @param projectName
     */
    void removeAllForComposeProject(String projectName) {
        LOGGER.info("compose: removing all for project named $projectName")
        def docker = new DockerWrapper();
        docker.getContainerIdsByFilter("label=com.docker.compose.project=$projectName").each {
            docker.removeContainer(it)
        }
        docker.getNetworkIdsByFilter("label=com.docker.compose.project=$projectName").each {
            docker.removeNetwork(it)
        }
    }


    String version() {
        (exec.executeForOutput("version") =~ /docker-compose version ([^\s]+),/)[0][1]
    }
}
