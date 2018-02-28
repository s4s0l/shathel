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
import org.s4s0l.shathel.commons.utils.ExtensionContext;

import java.util.List;
import java.util.Optional;

/**
 * @author Marcin Wielgus
 */
public class Stack {
    private final ExtensionContext extensionContext;
    private final StackLocator stackReference;
    private final DependencyManager dependencyManager;

    public Stack(ExtensionContext extensionContext, StackLocator stackReference,
                 DependencyManager dependencyManager) {
        this.extensionContext = extensionContext;
        this.stackReference = stackReference;
        this.dependencyManager = dependencyManager;
    }

    private StackContext getStackContext(boolean withOptional,Environment environment) {
        StackIntrospectionProvider.StackIntrospections allStacks = environment.getIntrospectionProvider().getAllStacks();
        StackTreeDescription stackTreeDescription = dependencyManager.downloadDependencies(stackReference, allStacks, withOptional);
        List<StackDescription> sidekicks = dependencyManager.getSidekicks(stackTreeDescription, allStacks);
        return new StackContext(
                stackTreeDescription,
                sidekicks,
                allStacks,
                environment);
    }

    public StackOperations createStartCommand(boolean withOptionalDependencies,Environment environment) {
        return getEnricherExecutor(withOptionalDependencies,environment)
                .createStartSchedule();
    }

    public StackOperations createStopCommand(boolean withDependencies, boolean withOptional,Environment environment) {
        return getEnricherExecutor(withOptional,environment)
                .createStopSchedule(withDependencies);
    }

    private StackEnricherExecutor getEnricherExecutor(boolean withOptional,Environment environment) {
        return new StackEnricherExecutor(extensionContext, getStackContext(withOptional,environment), withOptional);
    }


    public static class StackContext {
        private final StackTreeDescription stackTreeDescription;
        private final Environment environment;
        private final StackIntrospectionProvider.StackIntrospections introspections;

        StackContext(StackTreeDescription stackTreeDescription,
                     List<StackDescription> sidekicks,
                     StackIntrospectionProvider.StackIntrospections stackIntrospections,
                     Environment environment) {
            this.stackTreeDescription = stackTreeDescription;
            this.sidekicks = sidekicks;
            this.environment = environment;
            this.introspections = stackIntrospections;
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
