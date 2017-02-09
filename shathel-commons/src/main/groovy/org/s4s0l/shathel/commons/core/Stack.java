package org.s4s0l.shathel.commons.core;

import org.s4s0l.shathel.commons.core.environment.Environment;
import org.s4s0l.shathel.commons.core.stack.StackTreeDescription;
import org.s4s0l.shathel.commons.utils.ExtensionContext;

/**
 * @author Matcin Wielgus
 */
public class Stack {
    private final StackTreeDescription stackDescriptionTree;
    private final Environment environment;


    public Stack(StackTreeDescription stackDescriptionTree, Environment environment) {
        this.stackDescriptionTree = stackDescriptionTree;
        this.environment = environment;

    }

    public StackOperations createStartCommand(boolean forcefull) {
        return getStackOperationsFactory()
                .createStartSchedule(forcefull);
    }

    public StackOperations createStopCommand(boolean withDependencies) {
        return getStackOperationsFactory()
                .createStopSchedule(withDependencies);
    }

    private StackOperationsFactory getStackOperationsFactory() {
        return new StackOperationsFactory(stackDescriptionTree, environment);
    }

    public void run(StackOperations schedule) {
        new StackOperationsExecutor(environment.getProvisionExecutor(),
                environment.getContainerRunner(),
                environment.getExecutionDirectory()).execute(schedule);
    }

}
