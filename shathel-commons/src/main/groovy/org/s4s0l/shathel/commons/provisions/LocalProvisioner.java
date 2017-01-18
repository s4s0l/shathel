package org.s4s0l.shathel.commons.provisions;

import org.s4s0l.shathel.commons.core.provision.EnvironmentProvisionExecutor;
import org.s4s0l.shathel.commons.core.provision.EnvironmentProvisionExecutorContext;
import org.s4s0l.shathel.commons.core.provision.StackCommand;
import org.s4s0l.shathel.commons.core.stack.StackProvisionerDefinition;

import java.io.File;
import java.util.List;

/**
 * @author Matcin Wielgus
 */
public class LocalProvisioner implements EnvironmentProvisionExecutor {
    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(LocalProvisioner.class);
    private final File workingDir;

    public LocalProvisioner(File workingDir) {
        this.workingDir = workingDir;
    }


    @Override
    public EnvironmentProvisionExecutorContext createContext() {
        return new EnvironmentProvisionExecutorContextImpl(workingDir);
    }


    private class EnvironmentProvisionExecutorContextImpl implements EnvironmentProvisionExecutorContext {
        private final File workingFile;

        private EnvironmentProvisionExecutorContextImpl(File workingFile) {
            this.workingFile = workingFile;
        }

        @Override
        public void executeCommands(File dstStackDir, StackCommand stackCommand) {
            List<StackProvisionerDefinition> provisioners = stackCommand.getProvisioners();
            for (StackProvisionerDefinition provisioner : provisioners) {
                LOGGER.info("Running provisioner {} {}", provisioner.getName(), provisioner.getType());
            }

        }

        @Override
        public void close() throws Exception {
            LOGGER.info("Closed");
        }
    }
}
