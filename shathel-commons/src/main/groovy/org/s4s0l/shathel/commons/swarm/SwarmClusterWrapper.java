package org.s4s0l.shathel.commons.swarm;

import org.s4s0l.shathel.commons.docker.DockerWrapper;
import org.s4s0l.shathel.commons.machine.vbox.NetworkSettings;

import java.util.List;
import java.util.Map;

/**
 * @author Matcin Wielgus
 */
public interface SwarmClusterWrapper {

    List<String> getAllNodeNames();

    void start(String node);

    void stop(String node);

    String ssh(String node, String command);

    String sudo(String node, String command);

    void scp(String from, String to);



    void destroy();

    Node getNode(String nodeName);

    DockerWrapper getWrapperForNode(String node);

    String getNonRootUser();

    /**
     * @param machineName        name of machine
     * @param ns                 network setting s to use for ip generation
     * @param expectedIp         number to pass to ns to get ip address
     * @param registryMirrorHost url to registry mirror to set in engine daemon
     * @return ip of machine created
     */
    CreationResult createNodeIfNotExists(String machineName, NetworkSettings ns, int expectedIp, String registryMirrorHost);

    default boolean isInitialized(int managersCount, int workersCount) {
        return getAllNodeNames().size() >= managersCount + workersCount;
    }


    /**
     * returns DOCKER_* environment variables used to talk with
     * docker daemon running on given node
     *
     * @param node node name
     * @return see above
     */
    Map<String, String> getMachineEnvs(String node);


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
