package org.s4s0l.shathel.commons.core.provision;

import java.io.File;

/**
 * @author Matcin Wielgus
 */
public interface EnvironmentProvisionExecutorContext extends AutoCloseable {
    void executeCommands(File dstStackDir, StackCommand stackCommand);
}
