package org.s4s0l.shathel.commons.localcompose;

import org.s4s0l.shathel.commons.core.environment.EnvironmentContainerRunner;
import org.s4s0l.shathel.commons.core.environment.EnvironmentContainerRunnerContext;
import org.s4s0l.shathel.commons.core.environment.StackCommand;
import org.s4s0l.shathel.commons.core.stack.StackDescription;
import org.s4s0l.shathel.commons.docker.DockerComposeWrapper;
import org.slf4j.Logger;

import java.io.File;
import java.util.Map;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * @author Marcin Wielgus
 */
public class LocalEnvironmentContainerRunner implements EnvironmentContainerRunner {
    private static final Logger LOGGER = getLogger(LocalEnvironmentContainerRunner.class);


    @Override
    public EnvironmentContainerRunnerContext createContext() {
        return new Context();
    }

    private class Context implements EnvironmentContainerRunnerContext {

        @Override
        public void startContainers(StackCommand command, File composeFile) {
            DockerComposeWrapper dockerCompose = new DockerComposeWrapper();
            if (!dockerCompose.up(composeFile.getParentFile(), command.getDescription().getDeployName(), command.getEnvironment())) {
                throw new RuntimeException("Unable to start stack " + command.getDescription().getReference());
            }
        }

        @Override
        public void stopContainers(StackCommand command, File composeFile) {
            DockerComposeWrapper dockerCompose = new DockerComposeWrapper();
            if (!dockerCompose.down(composeFile.getParentFile(), command.getDescription().getDeployName(), command.getEnvironment())) {
                throw new RuntimeException("Unable to stop stack " + command.getDescription().getReference());
            }
        }

        @Override
        public void close() throws Exception {

        }
    }
}
