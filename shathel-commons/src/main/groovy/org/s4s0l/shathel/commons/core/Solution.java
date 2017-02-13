package org.s4s0l.shathel.commons.core;

import org.s4s0l.shathel.commons.core.dependencies.DependencyDownloader;
import org.s4s0l.shathel.commons.core.dependencies.DependencyManager;
import org.s4s0l.shathel.commons.core.environment.*;
import org.s4s0l.shathel.commons.core.model.SolutionFileModel;
import org.s4s0l.shathel.commons.core.security.LazyInitiableSafeStorage;
import org.s4s0l.shathel.commons.core.security.SafeStorageProvider;
import org.s4s0l.shathel.commons.core.stack.StackDescription;
import org.s4s0l.shathel.commons.core.stack.StackReference;
import org.s4s0l.shathel.commons.core.stack.StackTreeDescription;
import org.s4s0l.shathel.commons.core.storage.Storage;
import org.s4s0l.shathel.commons.utils.ExtensionContext;

import java.util.List;

/**
 * @author Matcin Wielgus
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


    public Environment getEnvironment(String environmentName) {
        SolutionDescription solutionDescription = getSolutionDescription();
        EnvironmentDescription environmentDescription = solutionDescription.getEnvironmentDescription(environmentName);
        String type = environmentDescription.getType();

        EnvironmentProvider environmentProvider = extensionContext
                .lookupOneMatching(EnvironmentProvider.class, x -> x.getType().equals(type))
                .get();

        String name = environmentDescription.getName();

        LazyInitiableSafeStorage safeStorage = new LazyInitiableSafeStorage(() ->
                extensionContext.lookupOne(SafeStorageProvider.class).get().getSafeStorage(storage, name));


        EnvironmentContext environmentContext = new EnvironmentContext(extensionContext, environmentDescription, solutionDescription,
                safeStorage, storage.getTemporaryDirectory(name), storage.getWorkDirectory(name));

        return environmentProvider.getEnvironment(environmentContext);
    }

    public SolutionDescription getSolutionDescription() {
        SolutionFileModel model = SolutionFileModel.load(storage.getConfiguration());
        return new SolutionDescription(params, model);
    }


    public Stack openStack(Environment e, StackReference reference, boolean forcefull) {
        e.verify();
        StackIntrospectionProvider stackIntrospectionProvider = e.getIntrospectionProvider();
        DependencyManager dependencyManager = getDependencyManager(stackIntrospectionProvider, forcefull);
        StackTreeDescription stackDescriptionTree = dependencyManager.downloadDependencies(reference);
        List<StackDescription> sidekicks = dependencyManager.getSidekicks(stackDescriptionTree);
        return new Stack(stackDescriptionTree, sidekicks, e);
    }

    private DependencyManager getDependencyManager(StackIntrospectionProvider stackIntrospectionProvider, boolean forcefull) {
        return new DependencyManager(
                storage.getTemporaryDirectory("dependencies"),
                extensionContext.lookupOne(DependencyDownloader.class).get(), stackIntrospectionProvider, forcefull);
    }


}
