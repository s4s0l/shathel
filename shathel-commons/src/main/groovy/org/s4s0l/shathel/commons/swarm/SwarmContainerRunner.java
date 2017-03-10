package org.s4s0l.shathel.commons.swarm;

import org.s4s0l.shathel.commons.core.environment.EnvironmentContainerRunner;
import org.s4s0l.shathel.commons.core.environment.EnvironmentContainerRunnerContext;
import org.s4s0l.shathel.commons.core.stack.StackDescription;
import org.s4s0l.shathel.commons.docker.DockerWrapper;

import java.io.File;
import java.util.Map;

/**
 * @author Marcin Wielgus
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
    public void startContainers(String deployName, Map<String,String> environment, File composeFile) {
        docker.stackDeploy(composeFile, deployName, environment);
    }

    @Override
    public void stopContainers(String deployName, Map<String,String> environment, File composeFile)
    {
        docker.stackUnDeploy(composeFile, deployName, environment);
    }

    @Override
    public void close() throws Exception {

    }

    private final DockerWrapper docker;
}
