package org.s4s0l.shathel.commons.localcompose;

import org.s4s0l.shathel.commons.core.environment.EnvironmentContainerRunner;
import org.s4s0l.shathel.commons.core.environment.EnvironmentContainerRunnerContext;
import org.s4s0l.shathel.commons.core.stack.StackDescription;
import org.s4s0l.shathel.commons.docker.DockerComposeWrapper;
import org.slf4j.Logger;

import java.io.File;
import java.util.Map;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * @author Marcin Wielgus
 */
public class LocalEnvironmentContainerRunner implements  EnvironmentContainerRunner {
    private static final Logger LOGGER = getLogger(LocalEnvironmentContainerRunner.class);



    @Override
    public EnvironmentContainerRunnerContext createContext() {
        return new Context();
    }

    private class Context implements EnvironmentContainerRunnerContext {

        @Override
        public void startContainers(String deployName, Map<String,String> environment, File composeFile) {
            DockerComposeWrapper dockerCompose = new DockerComposeWrapper();
            if(!dockerCompose.up(composeFile.getParentFile(), deployName, environment)){
                throw new RuntimeException("Unable to start stack " + deployName);
            }
        }

        @Override
        public void stopContainers(String deployName, Map<String,String> environment, File composeFile)
        {
            DockerComposeWrapper dockerCompose = new DockerComposeWrapper();
            if(!dockerCompose.down(composeFile.getParentFile(), deployName, environment)){
                throw new RuntimeException("Unable to stop stack " + deployName);
            }
        }

        @Override
        public void close() throws Exception {

        }
    }
}
