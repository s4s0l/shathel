package org.s4s0l.shathel.commons.core;

import org.s4s0l.shathel.commons.core.dependencies.DependencyDownloader;
import org.s4s0l.shathel.commons.core.dependencies.DependencyManager;
import org.s4s0l.shathel.commons.core.enricher.EnrichersFasade;
import org.s4s0l.shathel.commons.core.environment.*;
import org.s4s0l.shathel.commons.core.model.SolutionFileModel;
import org.s4s0l.shathel.commons.core.stack.StackReference;
import org.s4s0l.shathel.commons.core.stack.StackTreeDescription;
import org.s4s0l.shathel.commons.core.storage.Storage;
import org.s4s0l.shathel.commons.utils.ExtensionContext;
import org.s4s0l.shathel.commons.utils.VersionComparator;

import java.io.File;

/**
 * @author Matcin Wielgus
 */
public class Solution {
    private final ExtensionContext context;
    private final Parameters params;
    private final Storage storage;

    public Solution(ExtensionContext context, Parameters params, Storage storage) {
        this.context = context;
        this.params = params;
        this.storage = storage;
    }


    public Environment getEnvironment(String environmentName) {
        SolutionDescription solutionDescription = getSolutionDescription();
        EnvironmentDescription environmentDescription = solutionDescription.getEnvironmentDescription(environmentName);
        String type = environmentDescription.getType();

        EnvironmentProvider environmentProvider = context
                .lookupOneMatching(EnvironmentProvider.class, x -> x.getType().equals(type))
                .get();
        return environmentProvider.getEnvironment(storage, environmentDescription,
                context, solutionDescription);
    }

    public SolutionDescription getSolutionDescription() {
        SolutionFileModel model = SolutionFileModel.load(storage.getConfiguration());
        return new SolutionDescription(model);
    }


    public Stack openStack(Environment e, StackReference reference) {
        e.verify();
        StackIntrospectionProvider stackIntrospectionProvider = e.getIntrospectionProvider();
        EnrichersFasade enricherProvider = new EnrichersFasade(context);
        DependencyManager dependencyManager = getDependencyManager(stackIntrospectionProvider);
        StackTreeDescription stackDescriptionTree = dependencyManager.downloadDependencies(reference);
        return new Stack(stackDescriptionTree, e, enricherProvider);
    }

    private DependencyManager getDependencyManager(StackIntrospectionProvider stackIntrospectionProvider) {
        DependencyManager.VersionOverrider overrider = desc ->
                stackIntrospectionProvider.getIntrospection(desc)
                        .filter(x -> new VersionComparator().compare(x.getReference().getVersion(), desc.getVersion()) > 0)
                        .map(x -> x.getReference().getVersion())
                        .orElse(desc.getVersion());
        return new DependencyManager(
                storage.getTemporaryDirectory("dependencies"),
                context.lookupOne(DependencyDownloader.class).get(), overrider);
    }


}
