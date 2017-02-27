package org.s4s0l.shathel.commons.core.environment;

import org.s4s0l.shathel.commons.core.SolutionDescription;
import org.s4s0l.shathel.commons.core.security.SafeStorage;
import org.s4s0l.shathel.commons.core.storage.Storage;
import org.s4s0l.shathel.commons.utils.ExtensionContext;

import java.io.File;

/**
 * @author Matcin Wielgus
 */
public class EnvironmentContext {
    private final ExtensionContext extensionContext;
    private final EnvironmentDescription environmentDescription;
    private final SolutionDescription solutionDescription;
    private final SafeStorage safeStorage;
    private final Storage storage;

    public EnvironmentContext(ExtensionContext extensionContext, EnvironmentDescription environmentDescription,
                              SolutionDescription solutionDescription, SafeStorage safeStorage, Storage storage) {
        this.extensionContext = extensionContext;
        this.environmentDescription = environmentDescription;
        this.solutionDescription = solutionDescription;
        this.safeStorage = safeStorage;
        this.storage = storage;
    }

    public ExtensionContext getExtensionContext() {
        return extensionContext;
    }



    public SafeStorage getSafeStorage() {
        return safeStorage;
    }

    public EnvironmentDescription getEnvironmentDescription() {
        return environmentDescription;
    }

    public SolutionDescription getSolutionDescription() {
        return solutionDescription;
    }

    public File getSettingsDirectory() {
        return storage.getSettingsDirectory(getEnvironmentDescription(), getEnvironmentDescription().getName());
    }

    public File getDataDirectory() {
        return storage.getSettingsDirectory(getEnvironmentDescription(), getEnvironmentDescription().getName());
    }

    public File getTempDirectory() {
        return storage.getTemptDirectory(getEnvironmentDescription(), getEnvironmentDescription().getName());
    }

    public File getEnrichedDirectory() {
        return storage.getEnrichedDirectory(getEnvironmentDescription(), getEnvironmentDescription().getName());
    }

    public String getContextName() {
        return getSolutionDescription().getName() + "-" + getEnvironmentDescription().getName();
    }

    public File getDependencyCacheDirectory() {
        return storage.getDependencyCacheDirectory(getEnvironmentDescription(), getEnvironmentDescription().getName());
    }
}
