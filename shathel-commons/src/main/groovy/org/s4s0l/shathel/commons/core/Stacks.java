package org.s4s0l.shathel.commons.core;

import org.s4s0l.shathel.commons.core.dependencies.DependencyManager;
import org.s4s0l.shathel.commons.core.dependencies.StackLocator;
import org.s4s0l.shathel.commons.core.environment.Environment;
import org.s4s0l.shathel.commons.core.environment.StackIntrospectionProvider;
import org.s4s0l.shathel.commons.core.stack.StackTreeDescription;
import org.s4s0l.shathel.commons.utils.ExtensionContext;

import java.util.List;

/**
 * @author Marcin Wielgus
 */
public class Stacks {
    private final ExtensionContext extensionContext;
    private final List<StackLocator> stackReference;
    private final DependencyManager dependencyManager;

    Stacks(ExtensionContext extensionContext, List<StackLocator> stackReference,
           DependencyManager dependencyManager) {
        this.extensionContext = extensionContext;
        this.stackReference = stackReference;
        this.dependencyManager = dependencyManager;
    }

    private StackTreeDescription getStackContext(Environment environment) {
        StackIntrospectionProvider.StackIntrospections allStacks = environment.getIntrospectionProvider().getAllStacks();
        return dependencyManager.downloadDependencies(stackReference, allStacks);
    }

    public StackOperations createStartCommand(boolean withOptionalDependencies, Environment environment) {
        return getEnricherExecutor(withOptionalDependencies, environment)
                .createStartSchedule();
    }

    public StackOperations createStopCommand(boolean withDependencies, boolean withOptional, Environment environment) {
        return getEnricherExecutor(withOptional, environment)
                .createStopSchedule(withDependencies);
    }

    private StackEnricherExecutor getEnricherExecutor(boolean withOptional, Environment environment) {
        return new StackEnricherExecutor(extensionContext, environment, getStackContext(environment), withOptional);
    }

}
