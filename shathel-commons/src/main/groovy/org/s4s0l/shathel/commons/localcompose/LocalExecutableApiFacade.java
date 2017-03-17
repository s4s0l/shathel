package org.s4s0l.shathel.commons.localcompose;

import org.s4s0l.shathel.commons.core.environment.ExecutableApiFacade;
import org.s4s0l.shathel.commons.docker.DockerWrapper;
import org.s4s0l.shathel.commons.secrets.SecretManager;
import org.slf4j.Logger;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * @author Marcin Wielgus
 */
public class LocalExecutableApiFacade implements ExecutableApiFacade {
    private static final Logger LOGGER = getLogger(LocalExecutableApiFacade.class);
    private final DockerWrapper dockerWrapper;

    public LocalExecutableApiFacade(DockerWrapper dockerWrapper) {
        this.dockerWrapper = dockerWrapper;
    }


    @Override
    public List<String> getNodeNames() {
        return Collections.singletonList("localhost");
    }

    @Override
    public String getIp(String nodeName) {
        return "localhost";
    }

    @Override
    public String getIpForManagementNode() {
        return "localhost";
    }

    @Override
    public DockerWrapper getDockerForManagementNode() {
        return dockerWrapper;
    }

    @Override
    public String getNameForManagementNode() {
        return "localhost";
    }

    @Override
    public DockerWrapper getDocker(String nodeName) {
        if ("localhost".equals(nodeName)) {
            return getDockerForManagementNode();
        } else {
            throw new RuntimeException("Unknown node name " + nodeName);
        }
    }

    @Override
    public SecretManager getSecretManager() {
        throw new RuntimeException("Secrets not supported in compose environment");
    }

    @Override
    public void setKernelParam(String param) {
        LOGGER.warn("!Set parameter like: sudo sysctl -w " + param);
    }

    @Override
    public Optional<String> getRegistry() {
        return Optional.empty();
    }

    @Override
    public Map<String, String> getDockerEnvs(String nodeName) {
        return Collections.emptyMap();
    }


}
