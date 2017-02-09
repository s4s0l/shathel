package org.s4s0l.shathel.commons.core.provision;

import java.io.File;

/**
 * @author Matcin Wielgus
 */
public interface EnvironmentProvisionExecutorContext extends AutoCloseable {
    void executePreProvisioners(File dstStackDir, StackCommand stackCommand);

    void executePostProvisioners(File dstStackDir, StackCommand stackCommand);
}
