package org.s4s0l.shathel.commons.machine.vbox;

import org.s4s0l.shathel.commons.core.Parameters;
import org.s4s0l.shathel.commons.machine.MachineProvisioner;

import java.io.File;

/**
 * @author Matcin Wielgus
 */
public class VBoxMachineProvisioner implements MachineProvisioner {
    private final Parameters parameters;

    public VBoxMachineProvisioner(Parameters parameters) {
        this.parameters = parameters;
    }

    @Override
    public boolean createMachines(File workDir, String baseMachineName, String envName, int numberOfManagers, int numberOfWorkers) {
        String net = parameters.getParameter("shathel.vbox." + envName + ".net").orElse("20.20.20");
        return new VBoxSwarmCluster(workDir, baseMachineName, numberOfManagers, numberOfWorkers, net).createMachines();
    }
}
