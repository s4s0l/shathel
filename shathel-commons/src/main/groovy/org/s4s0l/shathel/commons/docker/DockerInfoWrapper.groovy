package org.s4s0l.shathel.commons.docker

/**
 * @author Matcin Wielgus
 */
class DockerInfoWrapper {
    Map info;
    String name;

    DockerInfoWrapper(Map info, String name) {
        this.info = info
        this.name = name;
    }

    boolean isManager() {
        def thisNodeInSwarm = info.Swarm.NodeID
        def managers = info.Swarm.RemoteManagers
        if (info.Swarm.LocalNodeState != "active"
                || info.Swarm.ControlAvailable != true
                || managers.find { it.NodeID == thisNodeInSwarm } == null) {
            return false
        }
        return true
    }

    boolean isSwarmWorker() {
        def managers = info.Swarm.RemoteManagers
        if (info.Swarm.LocalNodeState != "active"
                || info.Swarm.ControlAvailable != false
                || managers.isEmpty()) {
            return false
        }
        return true
    }

    boolean isSwarmActive() {
        return info.Swarm.LocalNodeState == "active" && (isSwarmWorker() || isManager())
    }

    String getSwarmClusterId() {
        return info.Swarm.Cluster.ID
    }

    String getSwarmNodeId() {
        return info.Swarm.NodeID
    }

    /**
     * NodeID -> Addr
     * @return
     */
    Map<String, String> getRemoteManagers() {
        HashMap<String, String> remoteManagers = new HashMap<>();

        List rm = info.Swarm.RemoteManagers;
        rm.each {
            if (!it.Addr.startsWith('0.0.0.0')) {
                remoteManagers.put(it.NodeID, it.Addr)
            }
        }
        return remoteManagers
    }

}
