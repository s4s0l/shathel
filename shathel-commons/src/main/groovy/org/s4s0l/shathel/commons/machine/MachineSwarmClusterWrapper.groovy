package org.s4s0l.shathel.commons.machine

import org.s4s0l.shathel.commons.core.swarm.SwarmClusterWrapper
import org.s4s0l.shathel.commons.docker.DockerWrapper

/**
 * @author Matcin Wielgus
 */
class MachineSwarmClusterWrapper implements SwarmClusterWrapper {
    private final File settingsDirectory;
    private final MachineSwarmClusterFlavour clusterFlavour;

    MachineSwarmClusterWrapper(File settingsDirectory, MachineSwarmClusterFlavour clusterFlavour) {
        this.settingsDirectory = settingsDirectory
        this.clusterFlavour = clusterFlavour
    }

    @Override
    List<String> getAllNodeNames() {
        return null
    }

    @Override
    void start(String node) {

    }

    @Override
    void stop(String node) {

    }

    @Override
    String ssh(String node, String command) {
        return null
    }

    @Override
    void scp(String from, String to) {

    }

    @Override
    void destroy(String node) {

    }

    @Override
    SwarmClusterWrapper.Node getNode(String nodeName) {
        return null
    }

    @Override
    DockerWrapper getWrapperForNode(String node) {
        return null
    }
}
