package org.s4s0l.shathel.commons.core.swarm;

import org.s4s0l.shathel.commons.core.environment.EnvironmentContainerRunner;
import org.s4s0l.shathel.commons.core.environment.EnvironmentContainerRunnerContext;
import org.s4s0l.shathel.commons.core.stack.StackDescription;
import org.s4s0l.shathel.commons.docker.DockerWrapper;

import java.io.File;

/**
 * @author Matcin Wielgus
 */
public class SwarmContainerRunner implements EnvironmentContainerRunner, EnvironmentContainerRunnerContext {
    public SwarmContainerRunner(DockerWrapper docker) {
        this.docker = docker;
    }

    @Override
    public EnvironmentContainerRunnerContext createContext() {
        return this;
    }

    @Override
    public void startContainers(StackDescription description, File composeFile) {
        docker.stackDeploy(composeFile, description.getDeployName());
    }

    @Override
    public void stopContainers(StackDescription description, File composeFile) {
        docker.stackUnDeploy(composeFile, description.getDeployName());
    }

    @Override
    public void close() throws Exception {

    }

    private final DockerWrapper docker;
}
