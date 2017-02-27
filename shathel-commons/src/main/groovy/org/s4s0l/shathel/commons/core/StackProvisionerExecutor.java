package org.s4s0l.shathel.commons.core;

import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.io.FileUtils;
import org.s4s0l.shathel.commons.core.environment.*;
import org.s4s0l.shathel.commons.core.model.ComposeFileModel;
import org.s4s0l.shathel.commons.core.stack.StackProvisionerDefinition;
import org.s4s0l.shathel.commons.scripts.Executable;
import org.s4s0l.shathel.commons.scripts.HttpApis;
import org.s4s0l.shathel.commons.scripts.ScriptExecutorProvider;
import org.s4s0l.shathel.commons.utils.IoUtils;

import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * @author Matcin Wielgus
 */
public class StackProvisionerExecutor {

    private final EnvironmentContext environmentContext;
    private final ExecutableApiFacade executableApiFacade;
    private final EnvironmentContainerRunner runner;

    public StackProvisionerExecutor(EnvironmentContext environmentContext, ExecutableApiFacade executableApiFacade, EnvironmentContainerRunner runner) {
        this.environmentContext = environmentContext;
        this.executableApiFacade = executableApiFacade;
        this.runner = runner;
    }

    public void execute(StackOperations schedule) {
        try {

            File executionDirectory = environmentContext.getEnrichedDirectory();
            FileUtils.deleteDirectory(executionDirectory);
            executionDirectory.mkdirs();


            try (EnvironmentContainerRunnerContext ecrc = runner.createContext()) {
                for (StackCommand stackCommand : schedule.getCommands()) {

                    File srcStackDirectory = stackCommand.getDescription().getStackResources().getStackDirectory();
                    File dstStackDir = new File(executionDirectory, stackCommand.getDescription().getReference().getStackDirecctoryName());
                    IoUtils.copyContents(srcStackDirectory, executionDirectory);
                    File composeFile = new File(dstStackDir, "stack/docker-compose.yml");
                    ComposeFileModel.dump(stackCommand.getComposeModel(), composeFile);
                    if (stackCommand.getType() != StackCommand.Type.STOP) {
                        executePreProvisioners(dstStackDir, stackCommand);
                        ecrc.startContainers(stackCommand.getDescription(), composeFile);
                        executePostProvisioners(dstStackDir, stackCommand);
                    } else {
                        ecrc.stopContainers(stackCommand.getDescription(), composeFile);
                    }
                }
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    public void executePreProvisioners(File dstStackDir, StackCommand stackCommand) {
        List<StackProvisionerDefinition> preProvisioners = stackCommand.getDescription().getPreProvisioners();
        executeProvisioners(dstStackDir,stackCommand, preProvisioners);
        for (Executable executable : stackCommand.getEnricherPreProvisioners()) {
            execute(dstStackDir, executable,stackCommand);
        }
    }

    public void executePostProvisioners(File dstStackDir, StackCommand stackCommand) {
        List<StackProvisionerDefinition> postProvisioners = stackCommand.getDescription().getPostProvisioners();
        executeProvisioners(dstStackDir, stackCommand, postProvisioners);

    }

    private void executeProvisioners(File dstStackDir,StackCommand stackCommand, List<StackProvisionerDefinition> postProvisioners) {
        for (StackProvisionerDefinition postProvisioner : postProvisioners) {
            Executable executable = ScriptExecutorProvider
                    .findExecutor(environmentContext.getExtensionContext(), postProvisioner)
                    .orElseThrow(() -> new RuntimeException("No executable fouind for " + postProvisioner));
            execute(dstStackDir, executable, stackCommand);
        }
    }

    private void execute(File dstStackDir, Executable executable, StackCommand stackCommand) {
        Map<String, Object> ctxt = new HashedMap();
        ctxt.put("context", environmentContext);
        ctxt.put("env", executableApiFacade);
        ctxt.put("command", stackCommand);
        ctxt.put("dir", dstStackDir);
        ctxt.put("http", new HttpApis());
        executable.execute(ctxt);
    }
}
