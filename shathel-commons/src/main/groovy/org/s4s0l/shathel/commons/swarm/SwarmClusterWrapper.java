package org.s4s0l.shathel.commons.swarm;

import org.s4s0l.shathel.commons.core.environment.EnvironmentApiFacade;
import org.s4s0l.shathel.commons.docker.DockerInfoWrapper;
import org.s4s0l.shathel.commons.docker.DockerWrapper;
import org.s4s0l.shathel.commons.machine.vbox.NetworkSettings;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * @author Matcin Wielgus
 */
public interface SwarmClusterWrapper extends EnvironmentApiFacade {


    void start(String node);

    void stop(String node);

    String ssh(String node, String command);

    String sudo(String node, String command);

    void scp(String from, String to);

    String getDataDirectory();

    void destroy();

    Node getNode(String nodeName);


    String getNonRootUser();


    @Override
    default String getIpForManagementNode() {
        return getManager()
                .map(x -> getIp(x.getName()))
                .orElseThrow(() -> new RuntimeException("Unable to find reachable swarm manager"));
    }

    @Override
    default DockerWrapper getDockerForManagementNode() {
        return getManager()
                .map(x -> getDocker(x.getName()))
                .orElseThrow(() -> new RuntimeException("Unable to find reachable swarm manager"));
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


    /**
     * @param machineName        name of machine
     * @param ns                 network setting s to use for ip generation
     * @param expectedIp         number to pass to ns to get ip address
     * @param registryMirrorHost url to registry mirror to set in engine daemon
     * @return ip of machine created
     */
    CreationResult createNodeIfNotExists(String machineName, NetworkSettings ns, int expectedIp, String registryMirrorHost);

    default boolean isInitialized(int managersCount, int workersCount) {
        return getNodeNames().size() >= managersCount + workersCount;
    }



    default void labelNode(String nodeName, Map<String, String> labels){
        getDocker(nodeName).swarmNodeSetLabels(nodeName, labels);
    }


    class CreationResult {
        private final String ip;
        private final boolean modified;

        public CreationResult(String ip, boolean modified) {
            this.ip = ip;
            this.modified = modified;
        }

        public String getIp() {
            return ip;
        }

        public boolean isModified() {
            return modified;
        }
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
