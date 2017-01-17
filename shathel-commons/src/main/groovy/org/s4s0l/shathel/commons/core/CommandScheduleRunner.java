package org.s4s0l.shathel.commons.core;

import org.s4s0l.shathel.commons.files.model.ComposeFileModel;
import org.s4s0l.shathel.commons.utils.IoUtils;

import java.io.File;

/**
 * @author Matcin Wielgus
 */
public class CommandScheduleRunner {

    private final EnvironmentProvisionExecutor executor;
    private final EnvironmentContainerRunner runner;
    private final Storage storage;


    public CommandScheduleRunner(EnvironmentProvisionExecutor executor, EnvironmentContainerRunner runner, Storage storage) {
        this.executor = executor;
        this.runner = runner;
        this.storage = storage;
    }

    public void run(StartCommandSchedule schedule) {
        File executionDir = storage.getExecutionDir();
        try {
            try (EnvironmentProvisionExecutorContext epec = executor.createContext(executionDir)) {
                try (EnvironmentContainerRunnerContext ecrc = runner.createContext(executionDir)) {
                    for (StackCommand stackCommand : schedule.getCommands()) {
                        File srcStackDirectory = stackCommand.getDescription().getStackResources().getStackDirectory();
                        File dstStackDir = new File(executionDir, stackCommand.getDescription().getReference().getStackDirecctoryName());
                        IoUtils.copyContents(srcStackDirectory, dstStackDir);
                        File composeFile = new File(dstStackDir, "stack/docker-compose.yml");
                        ComposeFileModel.dump(stackCommand.getMutableModel(), composeFile);
                        epec.executeCommands(dstStackDir, stackCommand);
                        ecrc.runContainers(stackCommand.getDescription(), composeFile);
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }
}
