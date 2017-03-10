package org.s4s0l.shathel.commons.core;

import org.s4s0l.shathel.commons.core.dependencies.DependencyManager;
import org.s4s0l.shathel.commons.core.environment.Environment;
import org.s4s0l.shathel.commons.core.environment.StackIntrospectionProvider;
import org.s4s0l.shathel.commons.core.stack.StackDescription;
import org.s4s0l.shathel.commons.core.stack.StackReference;
import org.s4s0l.shathel.commons.core.stack.StackTreeDescription;

import java.util.List;

/**
 * @author Matcin Wielgus
 */
public class Stack {
    private final StackReference stackReference;
    private final DependencyManager dependencyManager;
    private final Environment environment;

    public Stack(StackReference stackReference, DependencyManager dependencyManager, Environment environment) {
        this.stackReference = stackReference;
        this.dependencyManager = dependencyManager;
        this.environment = environment;
    }

    public StackContext getStackContext(){
        StackIntrospectionProvider.StackIntrospections allStacks = environment.getIntrospectionProvider().getAllStacks();
        StackTreeDescription stackTreeDescription = dependencyManager.downloadDependencies(stackReference, allStacks);
        List<StackDescription> sidekicks = dependencyManager.getSidekicks(stackTreeDescription, allStacks);
        return new StackContext(
                stackTreeDescription,sidekicks,allStacks,environment

        );
    }

    public Environment getEnvironment() {
        return environment;
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
        StackIntrospectionProvider.StackIntrospections allStacks = getEnvironment().getIntrospectionProvider().getAllStacks();
        return new StackEnricherExecutor( getStackContext());
    }

    public void run(StackOperations schedule) {
        new StackProvisionerExecutor(environment.getEnvironmentContext(),
                environment.getEnvironmentApiFacade(),
                environment.getContainerRunner()).execute(schedule);
    }

    public static class StackContext {
        private final StackTreeDescription stackTreeDescription;
        private final List<StackDescription> sidekicks;
        private final StackIntrospectionProvider.StackIntrospections stackIntrospections;
        private final Environment environment;

        public StackContext(StackTreeDescription stackTreeDescription, List<StackDescription> sidekicks, StackIntrospectionProvider.StackIntrospections stackIntrospections, Environment environment) {
            this.stackTreeDescription = stackTreeDescription;
            this.sidekicks = sidekicks;
            this.stackIntrospections = stackIntrospections;
            this.environment = environment;
        }

        public Environment getEnvironment() {
            return environment;
        }

        public StackTreeDescription getStackTreeDescription() {
            return stackTreeDescription;
        }

        public List<StackDescription> getSidekicks() {
            return sidekicks;
        }

        public StackIntrospectionProvider.StackIntrospections getStackIntrospections() {
            return stackIntrospections;
        }
    }

}
