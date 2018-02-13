package org.s4s0l.shathel.commons.core;

import org.s4s0l.shathel.commons.ExtensionContextsProvider;
import org.s4s0l.shathel.commons.core.dependencies.DependencyDownloaderRegistry;
import org.s4s0l.shathel.commons.core.dependencies.DependencyManager;
import org.s4s0l.shathel.commons.core.dependencies.StackLocator;
import org.s4s0l.shathel.commons.core.environment.*;
import org.s4s0l.shathel.commons.core.model.SolutionFileModel;
import org.s4s0l.shathel.commons.core.security.LazyInitiableSafeStorage;
import org.s4s0l.shathel.commons.core.security.SafeStorageProvider;
import org.s4s0l.shathel.commons.core.stack.StackReference;
import org.s4s0l.shathel.commons.core.storage.Storage;
import org.s4s0l.shathel.commons.utils.ExtensionContext;

import java.io.File;
import java.util.List;
import java.util.Set;

/**
 * @author Marcin Wielgus
 */
public class Solution {
    private final ExtensionContextsProvider extensionContextsProvider;
    private final Parameters params;
    private final Storage storage;

    public Solution(ExtensionContextsProvider extensionContextsProvider, Parameters params, Storage storage) {
        this.extensionContextsProvider = extensionContextsProvider;
        this.params = params;
        this.storage = storage;
    }

    private ExtensionContext getExtensionContext() {
        return extensionContextsProvider.create(getSolutionDescription().getParameters());
    }

    public Set<String> getEnvironments() {
        return getSolutionDescription().getEnvironments();
    }

    public Environment getEnvironment(String environmentName) {
        SolutionDescription solutionDescription = getSolutionDescription();
        EnvironmentDescription environmentDescription = solutionDescription.getEnvironmentDescription(environmentName);
        String type = environmentDescription.getType();

        EnvironmentProvider environmentProvider = getExtensionContext()
                .lookupOneMatching(EnvironmentProvider.class, x -> x.getType().equals(type))
                .get();

        String name = environmentDescription.getEnvironmentName();

        File safeDirectory = storage.getSafeDirectory(environmentDescription.getParameters(), name);

        LazyInitiableSafeStorage safeStorage = new LazyInitiableSafeStorage(() ->
                getExtensionContext().lookupOne(SafeStorageProvider.class).get().getSafeStorage(safeDirectory, name));


        EnvironmentContext environmentContext = new EnvironmentContextImpl(environmentDescription, solutionDescription,
                safeStorage, storage);

        return environmentProvider.getEnvironment(environmentDescription, environmentContext);
    }

    private SolutionDescription getSolutionDescription() {
        SolutionFileModel model = SolutionFileModel.load(storage.getConfiguration());
        return new SolutionDescription(params, model);
    }

    public Stack openStack(StackReference reference) {
        return openStack(new StackLocator(reference));
    }

    public Stack openStack(StackLocator reference) {

        Boolean forcefulDownloader = getSolutionDescription().getParameters().getParameterAsBoolean("shathel.solution.forceful").orElse(false);
        DependencyManager dependencyManager = getDependencyManager(forcefulDownloader);
        return new Stack(getExtensionContext(), reference, dependencyManager);
    }

    public StackOperations getPurgeCommand(Environment environment) {
        List<StackIntrospection> rootStacks = environment.getIntrospectionProvider().getAllStacks().getRootStacks();
        StackOperations.Builder builder = StackOperations.builder(environment);
        for (StackIntrospection rootStack : rootStacks) {
            Stack stack = openStack(rootStack.getReference());
            StackOperations stopCommand = stack.createStopCommand(true, true, environment);
            builder.add(stopCommand.getCommands());
        }
        return builder.build();
    }

    public void run(StackOperations schedule) {
        new StackProvisionerExecutor(schedule, getExtensionContext()).execute();
    }


    private DependencyManager getDependencyManager(boolean forcefull) {
        DependencyDownloaderRegistry dependencyDownloaderRegistry = new DependencyDownloaderRegistry(getExtensionContext());
        return new DependencyManager(
                storage.getDependencyCacheDirectory(getSolutionDescription().getParameters()),
                dependencyDownloaderRegistry, getSolutionDescription(), forcefull);
    }


}
