package org.s4s0l.shathel.commons.core;

import org.s4s0l.shathel.commons.core.enricher.EnrichersFasade;
import org.s4s0l.shathel.commons.core.environment.Environment;
import org.s4s0l.shathel.commons.core.stack.StackTreeDescription;

import java.io.File;

/**
 * @author Matcin Wielgus
 */
public class Stack {
    private final StackTreeDescription stackDescriptionTree;
    private final Environment environment;
    private final EnrichersFasade enricherProvider;
    private final File executionDir;

    public Stack(StackTreeDescription stackDescriptionTree, Environment environment,
                 EnrichersFasade enricherProvider, File executionDir) {
        this.stackDescriptionTree = stackDescriptionTree;
        this.environment = environment;
        this.enricherProvider = enricherProvider;
        this.executionDir = executionDir;
    }

    public StackOperations createStartCommand() {
        return new StackOperationsFactory(stackDescriptionTree,
                environment.getIntrospectionProvider(), enricherProvider)
                .createStartSchedule();
    }

    public StackOperations createStopCommand(boolean withDependencies) {
        return new StackOperationsFactory(stackDescriptionTree,
                environment.getIntrospectionProvider(), enricherProvider)
                .createStopSchedule(withDependencies);
    }

    public void run(StackOperations schedule) {
        new StackOperationsExecutor(environment.getProvisionExecutor(),
                environment.getContainerRunner(),
                executionDir).execute(schedule);
    }

}
