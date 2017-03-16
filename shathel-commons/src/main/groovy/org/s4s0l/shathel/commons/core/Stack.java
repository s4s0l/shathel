package org.s4s0l.shathel.commons.core;

import com.google.common.collect.Streams;
import org.s4s0l.shathel.commons.core.dependencies.DependencyManager;
import org.s4s0l.shathel.commons.core.dependencies.StackLocator;
import org.s4s0l.shathel.commons.core.environment.Environment;
import org.s4s0l.shathel.commons.core.environment.StackIntrospection;
import org.s4s0l.shathel.commons.core.environment.StackIntrospectionProvider;
import org.s4s0l.shathel.commons.core.stack.StackDescription;
import org.s4s0l.shathel.commons.core.stack.StackReference;
import org.s4s0l.shathel.commons.core.stack.StackTreeDescription;

import java.util.List;
import java.util.Optional;

/**
 * @author Marcin Wielgus
 */
public class Stack {
    private final StackLocator stackReference;
    private final DependencyManager dependencyManager;
    private final Environment environment;

    public Stack(StackLocator stackReference, DependencyManager dependencyManager, Environment environment) {
        this.stackReference = stackReference;
        this.dependencyManager = dependencyManager;
        this.environment = environment;
    }

    public StackContext getStackContext(boolean withOptional) {
        StackIntrospectionProvider.StackIntrospections allStacks = environment.getIntrospectionProvider().getAllStacks();
        StackTreeDescription stackTreeDescription = dependencyManager.downloadDependencies(stackReference, allStacks, withOptional);
        List<StackDescription> sidekicks = dependencyManager.getSidekicks(stackTreeDescription, allStacks);
        return new StackContext(
                stackTreeDescription, sidekicks, allStacks, environment,
                allStacks);
    }

    public Environment getEnvironment() {
        return environment;
    }


    public StackOperations createStartCommand(boolean withOptionalDependencies) {
        return getEnricherExecutor(withOptionalDependencies)
                .createStartSchedule();
    }

    public StackOperations createStopCommand(boolean withDependencies, boolean withOptional) {
        return getEnricherExecutor(withOptional)
                .createStopSchedule(withDependencies);
    }

    private StackEnricherExecutor getEnricherExecutor(boolean withOptional) {
        return new StackEnricherExecutor(getStackContext(withOptional), withOptional);
    }

    public void run(StackOperations schedule) {
        new StackProvisionerExecutor(environment.getEnvironmentContext(),
                environment.getEnvironmentApiFacade(),
                environment.getContainerRunner()).execute(schedule);
    }

    public static class StackContext {
        private final StackTreeDescription stackTreeDescription;
        private final List<StackDescription> sidekicks;
        private final Environment environment;
        private final StackIntrospectionProvider.StackIntrospections introspections;

        public StackContext(StackTreeDescription stackTreeDescription, List<StackDescription> sidekicks, StackIntrospectionProvider.StackIntrospections stackIntrospections, Environment environment, StackIntrospectionProvider.StackIntrospections introspections) {
            this.stackTreeDescription = stackTreeDescription;
            this.sidekicks = sidekicks;
            this.environment = environment;
            this.introspections = introspections;
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

        public Optional<StackDescription> getStackDescription(StackReference stackReference) {
            return Streams.concat(stackTreeDescription.stream(), sidekicks.stream())
                    .filter(x -> x.getReference().isSameStack(stackReference)).findFirst();
        }

        public Optional<StackIntrospection> getCurrentlyRunning(StackReference reference) {
            return introspections.getIntrospection(reference);
        }

    }

}
