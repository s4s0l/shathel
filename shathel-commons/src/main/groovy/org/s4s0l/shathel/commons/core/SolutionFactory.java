package org.s4s0l.shathel.commons.core;

import org.s4s0l.shathel.commons.core.dependencies.DependencyDownloader;
import org.s4s0l.shathel.commons.core.dependencies.DependencyManager;
import org.s4s0l.shathel.commons.core.enricher.EnrichersFasade;
import org.s4s0l.shathel.commons.core.environment.Environment;
import org.s4s0l.shathel.commons.core.environment.EnvironmentDescription;
import org.s4s0l.shathel.commons.core.environment.EnvironmentFactory;
import org.s4s0l.shathel.commons.core.model.SolutionFileModel;
import org.s4s0l.shathel.commons.core.storage.Storage;
import org.s4s0l.shathel.commons.core.storage.StorageProvider;
import org.s4s0l.shathel.commons.utils.ExtensionContext;

import java.io.File;

/**
 * @author Matcin Wielgus
 */
public class SolutionFactory {
    private final ExtensionContext extensionContext;

    public SolutionFactory(ExtensionContext context) {
        extensionContext = context;
    }


    public Solution open(File directory, String environmentName) {
        SolutionFileModel model;
        if (new File(directory, "shthl-solution.yml").exists()) {
            model = SolutionFileModel.load(new File(directory, "shthl-solution.yml"));
        } else {
            model = SolutionFileModel.empty();
        }
        SolutionDescription solutionDescription = new SolutionDescription(model);
        EnvironmentDescription environmentDescription = solutionDescription.getEnvironmentDescription(environmentName);
        Storage s = extensionContext.lookupOne(StorageProvider.class).get().getStorage( directory);

        Environment environment = new EnvironmentFactory(extensionContext).getEnvironment(environmentDescription, s);
        DependencyManager dependencyManager = new DependencyManager(extensionContext.lookupOne(DependencyDownloader.class).get());
        EnrichersFasade enricherProvider = new EnrichersFasade(extensionContext);

        return new Solution(solutionDescription, environment, enricherProvider, dependencyManager, s);
    }
}
