package org.s4s0l.shathel.commons.machine

import org.s4s0l.shathel.commons.core.environment.EnvironmentContext
import org.s4s0l.shathel.commons.docker.DockerMachineWrapper
import org.s4s0l.shathel.commons.docker.DockerWrapper
import org.s4s0l.shathel.commons.machine.vbox.NetworkSettings
import org.s4s0l.shathel.commons.swarm.SwarmClusterWrapper
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
    List<String> getNodeNames() {
        def collect = getWrapper().getMachines().collect { it.key }
        Collections.sort(collect)
        return collect;
    }

    @Override
    String getIp(String nodeName) {
        return getWrapper().getIp(nodeName);
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
        getNodeNames().each {
            getWrapper().remove(it)
        }
    }

    @Override
    String getDataDirectory() {
        return "/mnt/sda1/shathel-data";
    }

    @Override
    SwarmClusterWrapper.Node getNode(String nodeName) {
        try {
            def machines = getWrapper().getMachines()[nodeName]
            return new SwarmClusterWrapper.Node(
                    machines.name,
                    machines.state == "Running",
                    getDocker(nodeName).isReachable())
        }catch(Exception e){
            //todo log?
            LOGGER.trace("Not neccecary an error", e);
            return new SwarmClusterWrapper.Node(
                    nodeName,
                    false,
                    false);
        }

    }

    String getNonRootUser() {
        return "docker"
    }

    @Override
    void setKernelParam(String param) {
        getNodeNames().each { node ->
            getWrapper().sudo(node, "sysctl -w $param")
            String cont = getWrapper().sudo(node, "cat /etc/sysctl.conf")
            def name = param.split("=")[0]
            if (cont.contains("${name}=")) {
                getWrapper().sudo(node, "sed -i.bak s/${name}=.*/$param/g /etc/sysctl.conf")
            } else {
                getWrapper().exec.executeForOutput(null, new File("."), [:], "ssh", node, "sudo", "/bin/sh", "-c",
                        "\"echo '$param' >> /etc/sysctl.conf\"")
            }
            cont = getWrapper().sudo(node, "cat /var/lib/boot2docker/profile")
            if (cont.contains("sysctl -w ${name}=")) {
                getWrapper().exec.executeForOutput(null,new File("."), [:], "ssh", node, "sudo", "/bin/sh", "-c",
                        "\"sed -i.bak s/sysctl\\ -w\\ ${name}=.*/sysctl\\ -w\\ $param/g /var/lib/boot2docker/profile\"")
            } else {
                getWrapper().exec.executeForOutput(null,new File("."), [:], "ssh", node, "sudo", "/bin/sh", "-c",
                        "\"echo 'sysctl -w $param' >> /var/lib/boot2docker/profile\"")
            }

        }
    }

    @Override
    DockerWrapper getDocker(String node) {
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
    Map<String, String> getDockerEnvs(String node) {
        return getWrapper().getMachineEnvs(node)
    }

}
