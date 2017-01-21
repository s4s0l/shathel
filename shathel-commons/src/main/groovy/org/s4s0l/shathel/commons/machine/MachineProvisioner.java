package org.s4s0l.shathel.commons.machine;

import java.io.File;

/**
 * @author Matcin Wielgus
 */
public interface MachineProvisioner {

    boolean createMachines(File workDir, String baseMachineName, String envName, int numberOfManagers, int numberOfWorkers);

}
