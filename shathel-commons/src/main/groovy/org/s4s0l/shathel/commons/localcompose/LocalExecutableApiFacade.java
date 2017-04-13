package org.s4s0l.shathel.commons.localcompose;

import org.s4s0l.shathel.commons.core.environment.ExecutableApiFacade;
import org.s4s0l.shathel.commons.core.environment.ShathelNode;
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
@Deprecated
public class LocalExecutableApiFacade implements ExecutableApiFacade {
    private static final Logger LOGGER = getLogger(LocalExecutableApiFacade.class);
    private final DockerWrapper dockerWrapper;

    public LocalExecutableApiFacade(DockerWrapper dockerWrapper) {
        this.dockerWrapper = dockerWrapper;
    }


    public List<String> getNodeNames() {
        return Collections.singletonList("localhost");
    }

    public String getIp(String nodeName) {
        return "localhost";
    }

    public String getIpForManagementNode() {
        return "localhost";
    }

    public DockerWrapper getDockerForManagementNode() {
        return dockerWrapper;
    }

    public String getNameForManagementNode() {
        return "localhost";
    }

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

    public void setKernelParam(String param) {
        LOGGER.warn("!Set parameter like: sudo sysctl -w " + param);
    }

    public Optional<String> getRegistry() {
        return Optional.empty();
    }

    public Map<String, String> getDockerEnvs(String nodeName) {
        return Collections.emptyMap();
    }

    @Override
    public List<ShathelNode> getNodes() {
        return Collections.singletonList(new ShathelNode("localhost", "127.0.0.1", "127.0.0.1", "manager"));
    }

    @Override
    public DockerWrapper getDocker(ShathelNode nodeName) {
        return getDocker(nodeName.getNodeName());
    }

    @Override
    public Map<String, String> getDockerEnvs(ShathelNode nodeName) {
        return getDockerEnvs(nodeName.getNodeName());
    }

    @Override
    public String openPublishedPort(int port) {
        return "127.0.0.1:" + port;
    }

    @Override
    public ShathelNode getManagerNode() {
        return getNodes().get(0);
    }
}
