package org.s4s0l.shathel.commons.core;

import org.s4s0l.shathel.commons.core.environment.Environment;
import org.s4s0l.shathel.commons.core.stack.StackDescription;
import org.s4s0l.shathel.commons.core.stack.StackTreeDescription;

import java.util.List;

/**
 * @author Matcin Wielgus
 */
public class Stack {
    private final StackTreeDescription stackDescriptionTree;
    private final List<StackDescription> sidekicks;
    private final Environment environment;


    public Stack(StackTreeDescription stackDescriptionTree, List<StackDescription> sidekicks, Environment environment) {
        this.stackDescriptionTree = stackDescriptionTree;
        this.sidekicks = sidekicks;
        this.environment = environment;

    }

    public StackOperations createStartCommand() {
        return getStackOperationsFactory()
                .createStartSchedule();
    }

    public StackOperations createStopCommand(boolean withDependencies) {
        return getStackOperationsFactory()
                .createStopSchedule(withDependencies);
    }

    private StackOperationsFactory getStackOperationsFactory() {
        return new StackOperationsFactory(stackDescriptionTree, sidekicks, environment);
    }

    public void run(StackOperations schedule) {
        new StackOperationsExecutor(environment.getProvisionExecutor(),
                environment.getContainerRunner(),
                environment.getExecutionDirectory()).execute(schedule);
    }

}
