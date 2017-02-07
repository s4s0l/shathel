package org.s4s0l.shathel.commons.machine

import org.s4s0l.shathel.commons.core.environment.EnvironmentContext
import org.s4s0l.shathel.commons.swarm.SwarmClusterWrapper
import org.s4s0l.shathel.commons.docker.DockerMachineWrapper
import org.s4s0l.shathel.commons.docker.DockerWrapper
import org.s4s0l.shathel.commons.machine.vbox.NetworkSettings
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * @author Matcin Wielgus
 */
class MachineSwarmClusterWrapper implements SwarmClusterWrapper {
    private final EnvironmentContext environmentContext;
    private final MachineSwarmClusterFlavour clusterFlavour;

    MachineSwarmClusterWrapper(EnvironmentContext environmentContext, MachineSwarmClusterFlavour clusterFlavour) {
        this.environmentContext = environmentContext
        this.clusterFlavour = clusterFlavour
    }

    DockerMachineWrapper getWrapper() {
        return new DockerMachineWrapper(environmentContext.getSettingsDirectory());
    }

    @Override
    List<String> getAllNodeNames() {
        return getWrapper().getMachines().collect { it.key }
    }

    @Override
    void start(String node) {
        getWrapper().start(node)
    }

    @Override
    void stop(String node) {
        getWrapper().stop(node)
    }

    @Override
    String ssh(String node, String command) {
        return getWrapper().ssh(node, command)
    }

    @Override
    String sudo(String node, String command) {
        return getWrapper().sudo(node, command)
    }

    @Override
    void scp(String from, String to) {
        getWrapper().copy(from, to)
    }

    @Override
    void destroy() {
        getAllNodeNames().each {
            getWrapper().remove(it)
        }
    }

    @Override
    SwarmClusterWrapper.Node getNode(String nodeName) {
        def machines = getWrapper().getMachines()[nodeName]
        return new SwarmClusterWrapper.Node(
                machines.name,
                machines.state == "Running",
                getWrapperForNode(nodeName).isReachable()
        )
    }

    String getNonRootUser(){
        return "docker"
    }

    @Override
    DockerWrapper getWrapperForNode(String node) {
        return getWrapper().getDockerWrapperOn(node)
    }
    private static
    final Logger LOGGER = LoggerFactory.getLogger(MachineSwarmClusterWrapper.class);

    private void log(String msg) {
        LOGGER.info(msg)
    }

    @Override
    SwarmClusterWrapper.CreationResult createNodeIfNotExists(String machineName, NetworkSettings ns, int expectedIp, String registryMirrorHost) {
        log "Create node named $machineName"
        boolean modified = false
        if (getWrapper().getMachinesByName(machineName).isEmpty()) {
            String MACHINE_OPTS = clusterFlavour.getMachineOpts(ns)
            getWrapper().create("${MACHINE_OPTS} --engine-registry-mirror ${registryMirrorHost} $machineName")
            modified = true
        }
        def ip = clusterFlavour.staticIp(getWrapper(), environmentContext.getTempDirectory(), machineName, ns, expectedIp)
        return new SwarmClusterWrapper.CreationResult(ip.ip, modified || ip.modified)
    }

    @Override
    Map<String, String> getMachineEnvs(String node) {
        return getWrapper().getMachineEnvs(node)
    }
}
