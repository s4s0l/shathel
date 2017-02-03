package org.s4s0l.shathel.commons.core.swarm;

import java.io.File;

/**
 * @author Matcin Wielgus
 */
public interface NodeProvisioner {

    boolean createMachines(File workDir, String baseMachineName, String envName, int numberOfManagers, int numberOfWorkers);

}
