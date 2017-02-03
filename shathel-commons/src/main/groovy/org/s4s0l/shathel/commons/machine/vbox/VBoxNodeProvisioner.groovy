package org.s4s0l.shathel.commons.machine.vbox;

import org.s4s0l.shathel.commons.core.Parameters;
import org.s4s0l.shathel.commons.core.swarm.NodeProvisioner;
import org.s4s0l.shathel.commons.machine.MachineSwarmClusterWrapper;

import java.io.File;

/**
 * @author Matcin Wielgus
 */
class VBoxNodeProvisioner implements NodeProvisioner {
    private final Parameters parameters;
    private final MachineSwarmClusterWrapper clusterWrapper;

    VBoxNodeProvisioner(Parameters parameters, MachineSwarmClusterWrapper clusterWrapper) {
        this.parameters = parameters;
        this.clusterWrapper = clusterWrapper;
    }

    @Override
    boolean createMachines(File workDir, String baseMachineName, String envName, int numberOfManagers, int numberOfWorkers) {
        String net = parameters.getParameter("shathel.vbox." + envName + ".net").orElse("20.20.20");
        return new VBoxSwarmCluster(workDir, baseMachineName, numberOfManagers, numberOfWorkers, net).createMachines();
    }
}
