package org.s4s0l.shathel.commons.core;

import org.s4s0l.shathel.commons.files.model.SolutionFileModel;
import org.s4s0l.shathel.commons.utils.VersionComparator;

import java.io.File;

/**
 * @author Matcin Wielgus
 */
public class SolutionOpener {
    private final EnvironmentProvider environmentProvider;
    private final StorageProvider storageProvider;
    private final DependencyManager dependencyManager;
    private final EnricherProvider enricherProvider;

    public SolutionOpener(EnvironmentProvider environmentProvider, StorageProvider storageProvider, DependencyDownloader dependencyDownloader, EnricherProvider enricherProvider) {
        this.environmentProvider = environmentProvider;
        this.storageProvider = storageProvider;
        dependencyManager = new DependencyManager(dependencyDownloader);
        this.enricherProvider = enricherProvider;
    }


    public Solution open(File directory, String environmentName, StackReference reference) {
        SolutionFileModel model;
        if (new File(directory, "shthl-solution.yml").exists()) {
            model = SolutionFileModel.load(new File(directory, "shthl-solution.yml"));
        } else {
            model = SolutionFileModel.empty();
        }
        SolutionDescription solutionDescription = new SolutionDescription(model);
        Storage s = storageProvider.getStorage(directory);
        EnvironmentDescription environmentDescription = solutionDescription.getEnvironmentDescription(environmentName);
        Environment e = environmentProvider.getEnvironment(environmentDescription, s);
        File dependenciesDir = s.getDependenciesDir();

        StackIntrospectionProvider stackIntrospectionProvider = e.getIntrospectionProvider();

        DependencyManager.VersionOverrider overrider = desc ->
                stackIntrospectionProvider.getIntrospection(desc)
                        .filter(x -> new VersionComparator().compare(x.getReference().getVersion(), desc.getVersion())>0)
                        .map(x -> x.getReference().getVersion())
                        .orElse(desc.getVersion());

        StackDescriptionTree stackDescriptionTree = dependencyManager.downloadDependencies(dependenciesDir, reference, overrider);
        return new Solution(solutionDescription, e, stackDescriptionTree, enricherProvider);
    }
}
