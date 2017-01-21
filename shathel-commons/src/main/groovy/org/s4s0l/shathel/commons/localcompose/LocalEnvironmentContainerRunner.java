package org.s4s0l.shathel.commons.localcompose;

import org.s4s0l.shathel.commons.core.environment.EnvironmentContainerRunner;
import org.s4s0l.shathel.commons.core.environment.EnvironmentContainerRunnerContext;
import org.s4s0l.shathel.commons.core.stack.StackDescription;
import org.s4s0l.shathel.commons.docker.DockerComposeWrapper;
import org.slf4j.Logger;

import java.io.File;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * @author Matcin Wielgus
 */
public class LocalEnvironmentContainerRunner implements  EnvironmentContainerRunner {
    private static final Logger LOGGER = getLogger(LocalEnvironmentContainerRunner.class);



    @Override
    public EnvironmentContainerRunnerContext createContext() {
        return new Context();
    }

    private class Context implements EnvironmentContainerRunnerContext {

        @Override
        public void startContainers(StackDescription description, File composeFile) {
            DockerComposeWrapper dockerCompose = new DockerComposeWrapper();
            if(!dockerCompose.up(composeFile.getParentFile(), description.getDeployName())){
                throw new RuntimeException("Unable to start stack " + description.getGav());
            }
        }

        @Override
        public void stopContainers(StackDescription description, File composeFile) {
            DockerComposeWrapper dockerCompose = new DockerComposeWrapper();
            if(!dockerCompose.down(composeFile.getParentFile(), description.getDeployName())){
                throw new RuntimeException("Unable to stop stack " + description.getGav());
            }
        }

        @Override
        public void close() throws Exception {

        }
    }
}
