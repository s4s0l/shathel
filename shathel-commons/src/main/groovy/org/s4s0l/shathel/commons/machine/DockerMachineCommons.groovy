package org.s4s0l.shathel.commons.machine

import org.s4s0l.shathel.commons.docker.DockerMachineWrapper

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

    List<String> getAllNodeNames() {
        getManagerNodeNames() + getWorkerNodeNames()
    }

    List<String> getManagerNodeNames() {
        getWrapper().getMachines()
                .findAll { it.key =~ /$baseMachineName-manager-[0-9]+/ }
                .collect { it.key }

    }


    List<String> getWorkerNodeNames() {
        getWrapper().getMachines()
                .findAll { it.key =~ /$baseMachineName-worker-[0-9]+/ }
                .collect { it.key }
    }


    void startAll() {
        getAllNodeNames().each {
            getWrapper().start(it)
        }
    }

    boolean isAllStarted() {
        Map machines = getWrapper().getMachines();
        machines.find { it.state != "Running" } == null
    }

    void stopAll() {
        getAllNodeNames().each {
            getWrapper().stop(it)
        }

    }

    void destroyAll() {
        getAllNodeNames().each {
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
            throw new RuntimeException("$machine was expected to be a swarm manager!")
        }
    }

    void testSwarmStatus() {
        Map nodes = getWrapper().getNodes("$baseMachineName-manager-1")
        assert ['Reachable', 'Leader'].contains(nodes["$baseMachineName-manager-1"].managerStatus)
        assert nodes
                .findAll { it.key.contains("-manager-") }
                .findAll {
            !['Reachable', 'Leader'].contains(it.value.managerStatus)
        }:
                "All nodes named $baseMachineName-manager-X must be managers in cluster"
        assert nodes
                .findAll { it.key.contains("-worker-") }
                .findAll {
            it.value.managerStatus != null
        }:
                "All nodes named $baseMachineName-worker-X must not be managers"


    }
}
