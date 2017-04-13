package org.s4s0l.shathel.commons.localswarm;

import org.apache.commons.lang.StringUtils;
import org.s4s0l.shathel.commons.core.environment.Environment;
import org.s4s0l.shathel.commons.core.environment.EnvironmentContainerRunner;
import org.s4s0l.shathel.commons.core.environment.EnvironmentContext;
import org.s4s0l.shathel.commons.core.environment.StackIntrospectionProvider;
import org.s4s0l.shathel.commons.docker.DockerWrapper;
import org.s4s0l.shathel.commons.scripts.NamedExecutable;
import org.s4s0l.shathel.commons.secrets.SecretsEnricher;
import org.s4s0l.shathel.commons.swarm.MandatoryEnvironmentsValidator;
import org.s4s0l.shathel.commons.swarm.SwarmBuildingEnricher;
import org.s4s0l.shathel.commons.swarm.SwarmContainerRunner;
import org.s4s0l.shathel.commons.swarm.SwarmStackIntrospectionProvider;
import org.slf4j.Logger;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * @author Marcin Wielgus
 */
public class LocalSwarmEnvironment implements Environment {
    private static final Logger LOGGER = getLogger(LocalSwarmEnvironment.class);
    private final EnvironmentContext context;
    private final DockerWrapper dockerWrapper = new DockerWrapper();

    public LocalSwarmEnvironment(EnvironmentContext context) {
        this.context = context;
    }

    @Override
    public boolean isInitialized() {

        return getDockerWrapper().swarmActive();
    }

    @Override
    public void initialize() {

        getDockerWrapper().swarmInit();
        String nodeName = getDockerWrapper().swarmNodes().keySet().iterator().next();
        Map<String, String> labels = new HashMap<>();
        labels.put("shathel.node.name", "manager-1");
        labels.put("shathel.node.main", "true");
        getDockerWrapper().swarmNodeSetLabels(nodeName, labels);

    }

    private DockerWrapper getDockerWrapper() {
        return dockerWrapper;
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
        if (!StringUtils.isEmpty(System.getenv("DOCKER_HOST"))) {
            throw new RuntimeException("DOCKER_HOST env var is not empty, not allowed in local swarm environment");
        }
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
        return new SwarmStackIntrospectionProvider(getDockerWrapper());
    }

    @Override
    public EnvironmentContainerRunner getContainerRunner() {
        return new SwarmContainerRunner(getDockerWrapper());
    }

    @Override
    public EnvironmentContext getEnvironmentContext() {
        return context;
    }

    @Override
    public LocalSwarmApiFacade getEnvironmentApiFacade() {
        return new LocalSwarmApiFacade(getDockerWrapper(), context);
    }

    @Override
    public List<NamedExecutable> getEnvironmentEnrichers() {
        return Arrays.asList(
                new LocalMountingEnricher(),
                new SwarmBuildingEnricher(getEnvironmentApiFacade().getRegistry()),
                new SecretsEnricher(),
                new MandatoryEnvironmentsValidator()
        );
    }
}
