package org.s4s0l.shathel.commons.core;

import org.apache.commons.io.FileUtils;
import org.s4s0l.shathel.commons.core.environment.EnvironmentContainerRunner;
import org.s4s0l.shathel.commons.core.environment.EnvironmentContainerRunnerContext;
import org.s4s0l.shathel.commons.core.model.ComposeFileModel;
import org.s4s0l.shathel.commons.core.provision.EnvironmentProvisionExecutor;
import org.s4s0l.shathel.commons.core.provision.EnvironmentProvisionExecutorContext;
import org.s4s0l.shathel.commons.core.provision.StackCommand;
import org.s4s0l.shathel.commons.utils.IoUtils;

import java.io.File;

/**
 * @author Matcin Wielgus
 */
public class StackOperationsExecutor {

    private final EnvironmentProvisionExecutor executor;
    private final EnvironmentContainerRunner runner;
    private final File executionDir;


    public StackOperationsExecutor(EnvironmentProvisionExecutor executor, EnvironmentContainerRunner runner, File executionDir) {
        this.executor = executor;
        this.runner = runner;
        this.executionDir = executionDir;
    }

    public void execute(StackOperations schedule) {
        try {

            FileUtils.deleteDirectory(executionDir);
            executionDir.mkdirs();

            try (EnvironmentProvisionExecutorContext epec = executor.createContext()) {
                try (EnvironmentContainerRunnerContext ecrc = runner.createContext()) {
                    for (StackCommand stackCommand : schedule.getCommands()) {

                        File srcStackDirectory = stackCommand.getDescription().getStackResources().getStackDirectory();
                        File dstStackDir = new File(executionDir, stackCommand.getDescription().getReference().getStackDirecctoryName());
                        IoUtils.copyContents(srcStackDirectory, executionDir);
                        File composeFile = new File(dstStackDir, "stack/docker-compose.yml");
                        ComposeFileModel.dump(stackCommand.getComposeModel(), composeFile);
                        if(stackCommand.getType() != StackCommand.Type.STOP){
                            epec.executePreProvisioners(dstStackDir, stackCommand);
                            ecrc.startContainers(stackCommand.getDescription(), composeFile);
                            epec.executePostProvisioners(dstStackDir, stackCommand);
                        }else {
                            ecrc.stopContainers(stackCommand.getDescription(), composeFile);
                        }
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }
}
