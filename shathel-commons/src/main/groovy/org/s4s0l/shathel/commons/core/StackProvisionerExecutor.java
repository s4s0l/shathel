package org.s4s0l.shathel.commons.core;

import org.apache.commons.io.FileUtils;
import org.s4s0l.shathel.commons.core.environment.*;
import org.s4s0l.shathel.commons.core.model.ComposeFileModel;
import org.s4s0l.shathel.commons.core.stack.StackProvisionerDefinition;
import org.s4s0l.shathel.commons.scripts.NamedExecutable;
import org.s4s0l.shathel.commons.scripts.HttpApis;
import org.s4s0l.shathel.commons.scripts.ScriptExecutorProvider;
import org.s4s0l.shathel.commons.utils.ExtensionContext;
import org.s4s0l.shathel.commons.utils.IoUtils;
import org.slf4j.Logger;

import java.io.File;
import java.util.List;
import java.util.Map;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * @author Marcin Wielgus
 */
public class StackProvisionerExecutor {
    private final StackOperations schedule;
    private final ExtensionContext extensionContext;


    public StackProvisionerExecutor(StackOperations schedule, ExtensionContext extensionContext) {
        this.schedule = schedule;
        this.extensionContext = extensionContext;
    }

    private Environment getEnvironment() {
        return schedule.getEnvironment();
    }


    private EnvironmentContext getEnvironmentContext() {
        return schedule.getEnvironment().getEnvironmentContext();
    }

    private EnvironmentContainerRunner getRunner() {
        return schedule.getEnvironment().getContainerRunner();
    }

    public void execute() {
        try {

            File executionDirectory = getEnvironmentContext().getEnrichedDirectory();
            FileUtils.deleteDirectory(executionDirectory);
            executionDirectory.mkdirs();


            try (EnvironmentContainerRunnerContext ecrc = getRunner().createContext()) {
                for (StackCommand stackCommand : schedule.getCommands()) {

                    File srcStackDirectory = stackCommand.getDescription().getStackResources().getStackDirectory();
                    File dstStackDir = new File(executionDirectory, stackCommand.getDescription().getReference().getStackDirecctoryName());
                    IoUtils.copyContents(srcStackDirectory, dstStackDir);
                    File composeFile = new File(dstStackDir, "stack/docker-compose.yml");
                    ComposeFileModel.dump(stackCommand.getComposeModel(), composeFile);
                    if (stackCommand.getType() != StackCommand.Type.STOP) {
                        executePreProvisioners(dstStackDir, stackCommand);
                        ecrc.startContainers(stackCommand, composeFile);
                        executePostProvisioners(dstStackDir, stackCommand);
                    } else {
                        ecrc.stopContainers(stackCommand, composeFile);
                    }
                }
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    public void executePreProvisioners(File dstStackDir, StackCommand stackCommand) {
        List<StackProvisionerDefinition> preProvisioners = stackCommand.getDescription().getPreProvisioners();
        executeProvisioners(dstStackDir, stackCommand, preProvisioners);
        for (NamedExecutable executable : stackCommand.getEnricherPreProvisioners()) {
            execute(dstStackDir, executable, stackCommand);
        }
    }

    public void executePostProvisioners(File dstStackDir, StackCommand stackCommand) {
        List<StackProvisionerDefinition> postProvisioners = stackCommand.getDescription().getPostProvisioners();
        executeProvisioners(dstStackDir, stackCommand, postProvisioners);

    }

    private void executeProvisioners(File dstStackDir, StackCommand stackCommand, List<StackProvisionerDefinition> postProvisioners) {
        for (StackProvisionerDefinition postProvisioner : postProvisioners) {
            NamedExecutable executable = ScriptExecutorProvider
                    .findExecutor(extensionContext, postProvisioner)
                    .orElseThrow(() -> new RuntimeException("No executable fouind for " + postProvisioner));
            execute(dstStackDir, executable, stackCommand);
        }
    }

    private static final Logger LOGGER = getLogger(StackProvisionerExecutor.class);

    private void execute(File dstStackDir, NamedExecutable executable, StackCommand stackCommand) {
        ProvisionerExecutableParams params = new ProvisionerExecutableParams(
                getEnvironmentContext(),
                getEnvironment().getEnvironmentApiFacade(),
                stackCommand,
                dstStackDir,
                LOGGER,
                new HttpApis(),
                stackCommand.getEnvironment(),
                getEnvironment().getEnvironmentApiFacade().getNodes(),
                getEnvironment().getAnsibleScriptContext());
        Map<String, Object> context = params.toMap();
        LOGGER.info("Provisioning with: {}.", executable.getName());
        executable.execute(context);
    }
}
