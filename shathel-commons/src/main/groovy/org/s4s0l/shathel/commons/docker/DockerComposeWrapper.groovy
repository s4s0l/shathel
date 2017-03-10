package org.s4s0l.shathel.commons.docker

import org.s4s0l.shathel.commons.utils.ExecWrapper
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * @author Marcin Wielgus
 */
class DockerComposeWrapper {
    private static
    final Logger LOGGER = LoggerFactory.getLogger(DockerComposeWrapper.class);

    final ExecWrapper exec = new ExecWrapper(LOGGER, 'docker-compose')

    boolean up(File project,  String projectName,Map<String,String> env = [:]) {
        LOGGER.info("compose: starting project $projectName from ${project.absolutePath}")
        exec.executeForExitValue(project, env, true, "-p $projectName up -d") == 0
    }

    boolean down(File project, String projectName,Map<String,String> env = [:]) {
        LOGGER.info("compose: stopping project $projectName from ${project.absolutePath}")
        exec.executeForExitValue(project, env, true, "-p $projectName down --remove-orphans") == 0
    }


    /**
     * removes all containers,networks for given docker-compose project name
     * @param projectName
     */
    void removeAllForComposeProject(String projectName) {
        LOGGER.info("compose: removing all for project named $projectName")
        def docker = new DockerWrapper();
        docker.containerIdsByFilter("label=com.docker.compose.project=$projectName").each {
            docker.containerRemove(it)
        }
        docker.networkIdsByFilter("label=com.docker.compose.project=$projectName").each {
            docker.networkRemove(it)
        }
    }


    String version() {
        (exec.executeForOutput("version") =~ /docker-compose version ([^\s]+),/)[0][1]
    }
}
