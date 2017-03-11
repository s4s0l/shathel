package org.s4s0l.shathel.commons.localswarm;

import org.s4s0l.shathel.commons.core.environment.*;
import org.s4s0l.shathel.commons.docker.DockerWrapper;
import org.s4s0l.shathel.commons.localcompose.LocalExecutableApiFacade;
import org.s4s0l.shathel.commons.localcompose.LocalMountingEnricher;
import org.s4s0l.shathel.commons.scripts.Executable;
import org.s4s0l.shathel.commons.secrets.SecretManager;
import org.s4s0l.shathel.commons.secrets.SecretsEnricher;
import org.s4s0l.shathel.commons.swarm.SwarmBuildingEnricher;
import org.s4s0l.shathel.commons.swarm.SwarmContainerRunner;
import org.s4s0l.shathel.commons.swarm.SwarmStackIntrospectionProvider;
import org.slf4j.Logger;

import java.util.*;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * @author Marcin Wielgus
 */
public class LocalSwarmEnvironment implements Environment {
    private static final Logger LOGGER = getLogger(LocalSwarmEnvironment.class);
    private final EnvironmentContext context;

    public LocalSwarmEnvironment(EnvironmentContext context) {
        this.context = context;
    }

    @Override
    public boolean isInitialized() {
        return new DockerWrapper().swarmActive();
    }

    @Override
    public void initialize() {
        new DockerWrapper().swarmInit();
        String nodeName = new DockerWrapper().swarmNodes().keySet().iterator().next();
        Map<String, String> labels = new HashMap<>();
        labels.put("shathel.node.name", "manager-1");
        labels.put("shathel.node.main", "true");
        new DockerWrapper().swarmNodeSetLabels(nodeName, labels);

    }

    @Override
    public void start() {

    }

    @Override
    public boolean isStarted() {
        return true;
    }

    @Override
    public void stop() {

    }

    @Override
    public void destroy() {

    }

    @Override
    public void verify() {
        if (!isInitialized()) {
            throw new RuntimeException("Not initialized. Local docker is not in swarm mode.");
        }
    }

    @Override
    public void save() {

    }

    @Override
    public void load() {

    }

    @Override
    public StackIntrospectionProvider getIntrospectionProvider() {
        return new SwarmStackIntrospectionProvider(new DockerWrapper());
    }

    @Override
    public EnvironmentContainerRunner getContainerRunner() {
        return new SwarmContainerRunner(new DockerWrapper());
    }

    @Override
    public EnvironmentContext getEnvironmentContext() {
        return context;
    }

    @Override
    public ExecutableApiFacade getEnvironmentApiFacade() {
        return new LocalExecutableApiFacade(new DockerWrapper()) {
            @Override
            public SecretManager getSecretManager() {
                return new SecretManager(context.getEnvironmentDescription(), getClientForManagementNode());
            }
        };
    }

    @Override
    public List<Executable> getEnvironmentEnrichers() {
        return Arrays.asList(
                new LocalMountingEnricher(),
                new SwarmBuildingEnricher(null),
                new SecretsEnricher()
        );
    }
}
