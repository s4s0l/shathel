package org.s4s0l.shathel.commons.machine

import org.s4s0l.shathel.commons.docker.DockerMachineWrapper
import org.s4s0l.shathel.commons.docker.DockerWrapper

/**
 * @author Matcin Wielgus
 */
class DockerMachineCommons {
    private final String baseMachineName;
    private final File settingsDir;

    DockerMachineCommons(String baseMachineName, File settingsDir) {
        this.baseMachineName = baseMachineName
        this.settingsDir = settingsDir
    }

    DockerMachineWrapper getWrapper() {
        return new DockerMachineWrapper(settingsDir);
    }

    enum Type {
        MANAGER, WORKER
    }

    Map<Type, List<String>> getMachines() {
        def machines = getWrapper().getMachines();
        [
                (Type.MANAGER): machines
                        .findAll { it.key =~ /$baseMachineName-manager-[0-9]+/ }
                        .collect { it.key },
                (Type.WORKER) : machines
                        .findAll { it.key =~ /$baseMachineName-worker-[0-9]+/ }
                        .collect { it.key },
        ]
    }


    void startAll() {
        getMachines()
                .collect { it.value }
                .flatten()
                .each {
            getWrapper().start(it)
        }
    }

    boolean isAllStarted() {
        Map machines = getWrapper().getMachines();
        !machines.isEmpty() && machines.find { it.value.state != "Running" } == null
    }

    void stopAll() {
        getMachines()
                .collect { it.value }
                .flatten()
                .each {
            getWrapper().stop(it)
        }

    }

    void destroyAll() {
        getMachines()
                .collect { it.value }
                .flatten()
                .each {
            getWrapper().remove(it)
        }
    }

    void testConnectivity(String machine) {
        getWrapper().isReachable(machine)
    }

    void testIsManager(String machine) {
        if (!getWrapper().isSwarmManager(machine)) {
            throw new RuntimeException("$machine was expected to be a swarm manager!")
        }
    }

    void testIsWorker(String machine) {
        if (!getWrapper().isSwarmWorker(machine)) {
            throw new RuntimeException("$machine was expected to be a swarm worker!")
        }
    }

    void testSwarmStatus() {
        Map nodes = getWrapper().getNodes("$baseMachineName-manager-1")
        assert ['Reachable', 'Leader'].contains(nodes["$baseMachineName-manager-1"].managerStatus)
        assert nodes
                .findAll { it.key.contains("-manager-") }
                .findAll {
            !['Reachable', 'Leader'].contains(it.value.managerStatus)
        }.isEmpty():
                "All nodes named $baseMachineName-manager-X must be managers in cluster"
        assert nodes
                .findAll { it.key.contains("-worker-") }
                .findAll {
            it.value.managerStatus != null
        }.isEmpty():
                "All nodes named $baseMachineName-worker-X must not be managers"


    }

    DockerWrapper getDockerWrapperForManagementNode() {
        return getWrapper().getDockerWrapperOn("$baseMachineName-manager-1");
    }
}
