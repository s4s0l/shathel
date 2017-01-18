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

    final ExecWrapper exec = new ExecWrapper(LOGGER,'docker-compose')

    boolean up(File project, String projectName) {
        exec.executeForExitValue(project, "-p $projectName up -d") == 0
    }

    boolean down(File project, String projectName) {
        exec.executeForExitValue(project, "-p $projectName down --remove-orphans")==0
    }

    String containers(File project, String projectName) {
        exec.executeForOutput(project, "-p $projectName ps -q")
    }


}
