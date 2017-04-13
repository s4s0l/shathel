package org.s4s0l.shathel.commons.swarm;

import org.s4s0l.shathel.commons.core.environment.EnvironmentContext;

import java.io.File;

/**
 * @author Marcin Wielgus
 */
@Deprecated
public interface NodeProvisioner {

    boolean createMachines(File workDir, EnvironmentContext environmentContext);

}
