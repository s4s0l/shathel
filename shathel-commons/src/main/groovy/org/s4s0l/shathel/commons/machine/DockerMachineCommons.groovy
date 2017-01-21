package org.s4s0l.shathel.commons.machine

import org.s4s0l.shathel.commons.core.security.SafeStorage
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

    }

    List<String> getManagerNodeNames() {

    }


    List<String> getWorkerNodeNames() {

    }


    void startAll() {

    }

    boolean isAllStarted() {


    }

    void stopAll() {


    }

    void destroyAll() {

    }

    void testConnectivity(String machine) {

    }

    void testIsManager(String machine) {


    }

    void testIsWorker(String machine) {

    }

    void testSwarmStatus() {


    }
}
