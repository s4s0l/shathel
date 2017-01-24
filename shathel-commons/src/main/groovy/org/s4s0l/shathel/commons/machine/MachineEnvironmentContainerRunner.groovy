package org.s4s0l.shathel.commons.machine

import org.s4s0l.shathel.commons.core.environment.EnvironmentContainerRunner
import org.s4s0l.shathel.commons.core.environment.EnvironmentContainerRunnerContext
import org.s4s0l.shathel.commons.core.stack.StackDescription
import org.s4s0l.shathel.commons.docker.DockerWrapper

/**
 * @author Matcin Wielgus
 */
class MachineEnvironmentContainerRunner implements EnvironmentContainerRunner, EnvironmentContainerRunnerContext {

    private final DockerMachineCommons docker;

    MachineEnvironmentContainerRunner(DockerMachineCommons docker) {
        this.docker = docker
    }

    @Override
    EnvironmentContainerRunnerContext createContext() {
        return this;
    }

    @Override
    void startContainers(StackDescription description, File composeFile) {
        docker.dockerWrapperForManagementNode.stackDeploy(composeFile, description.getDeployName())
    }


    @Override
    void stopContainers(StackDescription description, File composeFile) {
        docker.dockerWrapperForManagementNode.stackUnDeploy(composeFile, description.getDeployName())
    }

    @Override
    void close() throws Exception {

    }


}
