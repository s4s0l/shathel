package org.s4s0l.shathel.commons.machine

import org.s4s0l.shathel.commons.core.environment.EnvironmentContext
import org.s4s0l.shathel.commons.docker.DockerMachineCachingWrapper
import org.s4s0l.shathel.commons.docker.DockerMachineWrapper
import org.s4s0l.shathel.commons.swarm.SwarmClusterWrapper
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * @author Marcin Wielgus
 */
@Deprecated
class MachineSwarmClusterWrapper implements SwarmClusterWrapper {
    private final EnvironmentContext environmentContext;
    private final DockerMachineCachingWrapper dockerMachineWrapper;

    MachineSwarmClusterWrapper(EnvironmentContext environmentContext) {
        this.environmentContext = environmentContext
        this.dockerMachineWrapper = new DockerMachineCachingWrapper(environmentContext.getSettingsDirectory())
    }

    @Override
    void refreshCaches() {
        this.dockerMachineWrapper.clearCache()
    }

    @Override
    EnvironmentContext getEnvironmentContext() {
        return environmentContext;
    }

    @Override
    Map<String, SwarmClusterWrapper.Node> getAllNodes() {
        getWrapper().getMachines().collectEntries {
            [
                    (it.key):
                            new SwarmClusterWrapper.Node(
                                    it.value.name,
                                    it.value.isRunning(),
                                    it.value.docker,
                                    it.value.swarmInfo,
                                    it.value.ip,
                                    it.value.envs
                            )
            ]
        }
    }

    DockerMachineWrapper getWrapper() {
        return dockerMachineWrapper
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


    String getNonRootUser() {
        return "docker"
    }

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
                getWrapper().exec.executeForOutput(null, new File("."), [:], "ssh", node, "sudo", "/bin/sh", "-c",
                        "\"sed -i.bak s/sysctl\\ -w\\ ${name}=.*/sysctl\\ -w\\ $param/g /var/lib/boot2docker/profile\"")
            } else {
                getWrapper().exec.executeForOutput(null, new File("."), [:], "ssh", node, "sudo", "/bin/sh", "-c",
                        "\"echo 'sysctl -w $param' >> /var/lib/boot2docker/profile\"")
            }

        }
    }


    private static
    final Logger LOGGER = LoggerFactory.getLogger(MachineSwarmClusterWrapper.class);

    private void log(String msg) {
        LOGGER.info(msg)
    }


}
