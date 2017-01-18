package org.s4s0l.shathel.commons.localcompose;

import org.s4s0l.shathel.commons.core.environment.EnvironmentContainerRunner;
import org.s4s0l.shathel.commons.core.environment.EnvironmentContainerRunnerContext;
import org.s4s0l.shathel.commons.core.environment.StackIntrospection;
import org.s4s0l.shathel.commons.core.environment.StackIntrospectionProvider;
import org.s4s0l.shathel.commons.core.stack.StackDescription;
import org.s4s0l.shathel.commons.core.stack.StackReference;
import org.s4s0l.shathel.commons.docker.DockerComposeWrapper;
import org.s4s0l.shathel.commons.docker.DockerWrapper;
import org.slf4j.Logger;

import java.io.File;
import java.util.Map;
import java.util.Optional;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * @author Matcin Wielgus
 */
public class LocalCompose implements StackIntrospectionProvider, EnvironmentContainerRunner {
    private static final Logger LOGGER = getLogger(LocalCompose.class);

    @Override
    public Optional<StackIntrospection> getIntrospection(StackReference reference) {
        Map oneByFilter = new DockerWrapper().findLabelsOfOneByFilter("label=org.shathel.stack.ga=" + reference.getGroup() + ":" + reference.getName());
        if (oneByFilter.get("org.shathel.stack.ga") == null) {
            return Optional.empty();
        }
        String o = (String) oneByFilter.get("org.shathel.stack.gav");
        return Optional.of(new StackIntrospection(new StackReference(o), oneByFilter));
    }

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
