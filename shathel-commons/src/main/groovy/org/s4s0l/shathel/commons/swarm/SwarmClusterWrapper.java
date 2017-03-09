package org.s4s0l.shathel.commons.swarm;

import org.s4s0l.shathel.commons.core.environment.EnvironmentContext;
import org.s4s0l.shathel.commons.core.environment.ExecutableApiFacade;
import org.s4s0l.shathel.commons.docker.DockerInfoWrapper;
import org.s4s0l.shathel.commons.docker.DockerWrapper;
import org.s4s0l.shathel.commons.machine.vbox.NetworkSettings;
import org.s4s0l.shathel.commons.secrets.SecretManager;

import java.util.Map;
import java.util.Optional;

/**
 * @author Matcin Wielgus
 */
public interface SwarmClusterWrapper extends ExecutableApiFacade {


    void start(String node);

    void stop(String node);

    String ssh(String node, String command);

    String sudo(String node, String command);

    void scp(String from, String to);

    String getDataDirectory();

    void destroy();

    Node getNode(String nodeName);

    EnvironmentContext getEnvironmentContext();

    String getNonRootUser();

    @Override
    default String getNameForManagementNode() {
        return getManager()
                .map(x -> x.getName())
                .orElseThrow(() -> new RuntimeException("Unable to find reachable swarm manager"));
    }

    @Override
    default String getIpForManagementNode() {
        return getIp(getNameForManagementNode());
    }

    @Override
    default DockerWrapper getDockerForManagementNode() {
        return getDocker(getNameForManagementNode());
    }

    @Override
    default SecretManager getSecretManager() {
        return new SecretManager(getEnvironmentContext().getEnvironmentDescription(), getClientForManagementNode());
    }

    default Optional<DockerInfoWrapper> getManager() {
        return getNodeNames().stream()
                .sorted()
                .map(x -> getNode(x))
                .filter(x -> x.isStarted() && x.isReachable())
                .map(x -> new DockerInfoWrapper(getDocker(x.getName()).daemonInfo(), x.getName()))
                .filter(x -> x.isManager())
                .findFirst();
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




    class Node {
        private final String name;
        private final boolean started;
        private final boolean reachable;

        public Node(String name, boolean started, boolean reachable) {
            this.name = name;
            this.started = started;
            this.reachable = reachable;
        }

        public String getName() {
            return name;
        }

        public boolean isStarted() {
            return started;
        }

        public boolean isReachable() {
            return reachable;
        }
    }


}
