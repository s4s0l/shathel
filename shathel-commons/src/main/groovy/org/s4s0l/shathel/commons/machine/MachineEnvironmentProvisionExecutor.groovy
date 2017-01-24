package org.s4s0l.shathel.commons.machine

import org.s4s0l.shathel.commons.core.provision.EnvironmentProvisionExecutor
import org.s4s0l.shathel.commons.core.provision.EnvironmentProvisionExecutorContext
import org.s4s0l.shathel.commons.core.provision.StackCommand

/**
 * @author Matcin Wielgus
 */
class MachineEnvironmentProvisionExecutor implements EnvironmentProvisionExecutor, EnvironmentProvisionExecutorContext {
    @Override
    EnvironmentProvisionExecutorContext createContext() {
        return this
    }

    @Override
    void executeCommands(File dstStackDir, StackCommand stackCommand) {
        if (!stackCommand.provisioners.isEmpty())
            throw new UnsupportedOperationException()
    }

    @Override
    void close() throws Exception {

    }
}
