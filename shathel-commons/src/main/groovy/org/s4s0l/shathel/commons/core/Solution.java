package org.s4s0l.shathel.commons.core;

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
import java.util.Optional;
import java.util.Set;

/**
 * @author Marcin Wielgus
 */
public class Solution {
    private final ExtensionContext extensionContext;
    private final Parameters params;
    private final Storage storage;

    public Solution(ExtensionContext extensionContext, Parameters params, Storage storage) {
        this.extensionContext = extensionContext;
        this.params = params;
        this.storage = storage;
    }

    public Set<String> getEnvironments() {
        return getSolutionDescription().getEnvironments();
    }

    public Environment getEnvironment(String environmentName) {
        SolutionDescription solutionDescription = getSolutionDescription();
        EnvironmentDescription environmentDescription = solutionDescription.getEnvironmentDescription(environmentName);
        String type = environmentDescription.getType();

        EnvironmentProvider environmentProvider = extensionContext
                .lookupOneMatching(EnvironmentProvider.class, x -> x.getType().equals(type))
                .get();

        String name = environmentDescription.getName();

        File safeDirectory = storage.getSafeDirectory(environmentDescription, name);

        LazyInitiableSafeStorage safeStorage = new LazyInitiableSafeStorage(() ->
                extensionContext.lookupOne(SafeStorageProvider.class).get().getSafeStorage(safeDirectory, name));


        EnvironmentContext environmentContext = new EnvironmentContextImpl(environmentDescription, solutionDescription,
                safeStorage, storage);

        return environmentProvider.getEnvironment(environmentContext);
    }

    public SolutionDescription getSolutionDescription() {
        SolutionFileModel model = SolutionFileModel.load(storage.getConfiguration());
        return new SolutionDescription(params, model);
    }

    public Stack openStack(Environment e, StackReference reference) {
        return openStack(e, new StackLocator(reference));
    }

    public Stack openStack(Environment e, StackLocator reference) {
        e.verify();
        DependencyManager dependencyManager = getDependencyManager(e);
        return new Stack(extensionContext, reference, dependencyManager, e);
    }

    private DependencyManager getDependencyManager(Environment e) {
        Optional<Boolean> forceful = e.getEnvironmentContext().getEnvironmentDescription().getParameterAsBoolean("forceful");
        DependencyDownloaderRegistry dependencyDownloaderRegistry = new DependencyDownloaderRegistry(extensionContext);
        return new DependencyManager(
                e.getEnvironmentContext().getDependencyCacheDirectory(),
                dependencyDownloaderRegistry, getSolutionDescription(), forceful.orElse(false));
    }


}
