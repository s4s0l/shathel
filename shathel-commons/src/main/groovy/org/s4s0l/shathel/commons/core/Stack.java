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
        return getEnricherExecutor()
                .createStartSchedule();
    }

    public StackOperations createStopCommand(boolean withDependencies) {
        return getEnricherExecutor()
                .createStopSchedule(withDependencies);
    }

    private StackEnricherExecutor getEnricherExecutor() {
        return new StackEnricherExecutor(stackDescriptionTree, sidekicks, environment);
    }

    public void run(StackOperations schedule) {
        new StackProvisionerExecutor(environment.getEnvironmentContext(),
                environment.getEnvironmentApiFacade(),
                environment.getContainerRunner()).execute(schedule);
    }

}
