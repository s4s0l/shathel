package org.s4s0l.shathel.commons.core.swarm;

import org.s4s0l.shathel.commons.docker.DockerWrapper;

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

    void scp(String from, String to);

    void destroy(String node);

    Node getNode(String nodeName);

    DockerWrapper getWrapperForNode(String node);

    default boolean isInitialized(int managersCount, int workersCount){
        return getAllNodeNames().size() >= managersCount + workersCount;
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
