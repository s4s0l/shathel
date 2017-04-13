package org.s4s0l.shathel.commons.swarm;

import org.s4s0l.shathel.commons.core.environment.EnvironmentContext;
import org.s4s0l.shathel.commons.core.environment.ExecutableApiFacade;
import org.s4s0l.shathel.commons.core.environment.ShathelNode;
import org.s4s0l.shathel.commons.docker.DockerInfoWrapper;
import org.s4s0l.shathel.commons.docker.DockerWrapper;
import org.s4s0l.shathel.commons.secrets.SecretManager;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author Marcin Wielgus
 */
@Deprecated
public interface SwarmClusterWrapper extends ExecutableApiFacade {


    void start(String node);

    void stop(String node);

    String ssh(String node, String command);

    String sudo(String node, String command);

    void scp(String from, String to);

    String getDataDirectory();

    String getNonRootUser();

    void destroy();

    EnvironmentContext getEnvironmentContext();

    Map<String, Node> getAllNodes();

    @Override
    default List<ShathelNode> getNodes() {
        return getAllNodes().values().stream()
                .map(it -> toShathelNode(it))
                .collect(Collectors.toList());
    }

    default ShathelNode toShathelNode(Node it) {
        return new ShathelNode(it.name, it.ip, it.ip, it.getSwarmInfo().isManager() ? "manager" : "worker");
    }

    @Override
    default DockerWrapper getDocker(ShathelNode nodeName) {
        return getDocker(nodeName.getNodeName());
    }

    @Override
    default Map<String, String> getDockerEnvs(ShathelNode nodeName) {
        return getNode(nodeName.getNodeName()).envs;
    }

    @Override
    default String openPublishedPort(int port) {
        return getManagerNode().getPublicIp() + ":" + port;
    }

    @Override
    default ShathelNode getManagerNode() {
        return getManager().map(it -> toShathelNode(it)).get();
    }

    default String getIp(String nodeName) {
        return getNode(nodeName).ip;
    }

    default DockerWrapper getDocker(String nodeName) {
        return getNode(nodeName).docker;
    }


    default List<String> getNodeNames() {
        return getAllNodes().keySet().stream().collect(Collectors.toList());
    }

    default Node getNode(String nodeName) {
        return getAllNodes().get(nodeName);
    }


    default Map<String, String> getDockerEnvs(String nodeName) {
        return getNode(nodeName).envs;
    }


    default String getNameForManagementNode() {
        return getManager()
                .map(x -> x.getName())
                .orElseThrow(() -> new RuntimeException("Unable to find reachable swarm manager"));
    }


    default String getIpForManagementNode() {
        return getIp(getNameForManagementNode());
    }


    default DockerWrapper getDockerForManagementNode() {
        return getDocker(getNameForManagementNode());
    }

    @Override
    default SecretManager getSecretManager() {
        return new SecretManager(getEnvironmentContext().getEnvironmentDescription(), getManagerNodeClient());
    }

    default Optional<Node> getManager() {
        return getAllNodes().entrySet().stream()
                .sorted(Comparator.comparing(o -> o.getValue().getName()))
                .map(x -> x.getValue())
                .filter(x -> x.isStarted())
                .filter(x -> x.swarmInfo.isManager())
                .findFirst();
    }


    default Optional<String> getRegistry() {
        return Optional.ofNullable(getEnvironmentContext().getEnvironmentDescription().getParameter("registry").orElseGet(() ->
                getNodeLabels(getManagerNode()).getOrDefault("shathel.node.registry", null)));
    }

    default boolean isInitialized(int managersCount, int workersCount) {
        return getNodeNames().size() >= managersCount + workersCount;
    }

    default void labelNode(String managerNodeName, String nodeName, Map<String, String> labels) {
        getDocker(managerNodeName).swarmNodeSetLabels(nodeName, labels);
    }

    default void labelNode(String nodeName, Map<String, String> labels) {
        getDockerForManagementNode().swarmNodeSetLabels(nodeName, labels);
    }

    void refreshCaches();

    class Node {
        private final String name;
        private final boolean started;
        private final DockerWrapper docker;
        private final DockerInfoWrapper swarmInfo;
        private final String ip;
        private final Map<String, String> envs;

        public Node(String name, boolean started, DockerWrapper dockerOnNode, DockerInfoWrapper swarmInfo, String ip, Map<String, String> envs) {
            this.name = name;
            this.started = started;
            this.docker = dockerOnNode;
            this.swarmInfo = swarmInfo;
            this.ip = ip;
            this.envs = envs;
        }


        public String getName() {
            return name;
        }

        public boolean isStarted() {
            return started;
        }

        public DockerWrapper getDocker() {
            return docker;
        }

        public DockerInfoWrapper getSwarmInfo() {
            return swarmInfo;
        }

        public String getIp() {
            return ip;
        }

        public Map<String, String> getEnvs() {
            return envs;
        }
    }


}
