package org.s4s0l.shathel.commons.localswarm;

import org.s4s0l.shathel.commons.core.environment.EnvironmentContext;
import org.s4s0l.shathel.commons.core.environment.ExecutableApiFacade;
import org.s4s0l.shathel.commons.docker.DockerWrapper;
import org.s4s0l.shathel.commons.secrets.SecretManager;
import org.slf4j.Logger;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * @author Marcin Wielgus
 */
class LocalSwarmApiFacade implements ExecutableApiFacade {
    private static final Logger LOGGER = getLogger(LocalSwarmApiFacade.class)
    private final DockerWrapper dockerWrapper
    private final EnvironmentContext context


    LocalSwarmApiFacade(DockerWrapper dockerWrapper, EnvironmentContext context) {
        this.dockerWrapper = dockerWrapper
        this.context = context
    }

    @Override
    List<String> getNodeNames() {
        return dockerWrapper.swarmNodes().keySet().stream().collect(Collectors.toList())
    }

    @Override
    String getIp(String nodeName) {
        Map x = dockerWrapper.swarmNodeInspect(nodeName)
        return x.Status.Addr
    }

    @Override
    String getIpForManagementNode() {
        return getIp(getNameForManagementNode())
    }

    @Override
    DockerWrapper getDockerForManagementNode() {
        return dockerWrapper
    }

    @Override
    String getNameForManagementNode() {
        def currentNodeId = dockerWrapper.daemonInfo().Swarm.NodeID
        def matching = dockerWrapper.swarmNodes().values().findAll {
            it.id == currentNodeId
        }.collect { it.hostName }
        if (matching.size() == 1) {
            return matching.head()
        } else {
            throw new RuntimeException("There is sth wrong, multiple nodes have same ID?")
        }
    }

    @Override
    DockerWrapper getDocker(String nodeName) {
        if (nodeName == getNameForManagementNode()) {
            return getDockerForManagementNode()
        } else {
            throw new RuntimeException("Unable to create docker wrapper for node " + nodeName)
        }
    }

    @Override
    SecretManager getSecretManager() {
        return new SecretManager(context.getEnvironmentDescription(), getClientForManagementNode())
    }

    @Override
    void setKernelParam(String param) {
        LOGGER.warn("!Set parameter like: sudo sysctl -w " + param)
    }

    @Override
    Optional<String> getRegistry() {
        return Optional.ofNullable(context.getEnvironmentDescription().getParameter("registry").orElseGet {
            getNodeLabels(getNameForManagementNode()).getOrDefault("shathel.node.registry", null)
        })
    }

    @Override
    Map<String, String> getDockerEnvs(String nodeName) {
        //todo: from where get theese, from env? But they already are there is there a use case?
        return Collections.emptyMap()
    }


}
