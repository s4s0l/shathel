package org.s4s0l.shathel.commons.core;

import org.s4s0l.shathel.commons.core.dependencies.DependencyManager;
import org.s4s0l.shathel.commons.core.enricher.EnrichersFasade;
import org.s4s0l.shathel.commons.core.environment.Environment;
import org.s4s0l.shathel.commons.core.environment.StackIntrospectionProvider;
import org.s4s0l.shathel.commons.core.stack.StackReference;
import org.s4s0l.shathel.commons.core.stack.StackTreeDescription;
import org.s4s0l.shathel.commons.core.storage.Storage;
import org.s4s0l.shathel.commons.utils.VersionComparator;

import java.io.File;

/**
 * @author Matcin Wielgus
 */
public class Solution {
    private final SolutionDescription solutionDescription;
    private final Environment environment;
    private final EnrichersFasade enricherProvider;
    private final DependencyManager dependencyManager;
    private final Storage storage;


    public Solution(SolutionDescription solutionDescription, Environment environment, EnrichersFasade enricherProvider, DependencyManager dependencyManager, Storage storage) {
        this.solutionDescription = solutionDescription;
        this.environment = environment;
        this.enricherProvider = enricherProvider;
        this.dependencyManager = dependencyManager;
        this.storage = storage;
    }


    public Stack openStack(StackReference reference) {
        File dependenciesDir = storage.getDependenciesDir();

        StackIntrospectionProvider stackIntrospectionProvider = environment.getIntrospectionProvider();

        DependencyManager.VersionOverrider overrider = desc ->
                stackIntrospectionProvider.getIntrospection(desc)
                        .filter(x -> new VersionComparator().compare(x.getReference().getVersion(), desc.getVersion()) > 0)
                        .map(x -> x.getReference().getVersion())
                        .orElse(desc.getVersion());

        StackTreeDescription stackDescriptionTree = dependencyManager.downloadDependencies(dependenciesDir, reference, overrider);
        return new Stack(stackDescriptionTree, environment, enricherProvider, storage.getExecutionDir());
    }


}
