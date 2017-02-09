package org.s4s0l.shathel.commons.core.environment;

import org.s4s0l.shathel.commons.core.SolutionDescription;
import org.s4s0l.shathel.commons.core.security.SafeStorage;
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
    private final File rootDirectory;
    private final File workDirectory;

    public EnvironmentContext(ExtensionContext extensionContext, EnvironmentDescription environmentDescription, SolutionDescription solutionDescription, SafeStorage safeStorage, File rootFile, File workDirectory) {
        this.extensionContext = extensionContext;
        this.environmentDescription = environmentDescription;
        this.solutionDescription = solutionDescription;
        this.safeStorage = safeStorage;
        this.rootDirectory = rootFile;
        this.workDirectory = workDirectory;
    }

    public ExtensionContext getExtensionContext() {
        return extensionContext;
    }

    public File getRootDirectory() {
        validateDir(rootDirectory);
        return rootDirectory;
    }

    public File getWorkDirectory() {
        return validateDir(workDirectory);
    }

    private File validateDir(File dir) {
        if (dir.exists()) {
            if (!dir.isDirectory()) {
                throw new RuntimeException("Expected " + dir.getAbsolutePath() + " to be a directory");
            }
        } else {
            dir.mkdirs();
        }
        return dir;
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
        return validateDir(new File(getRootDirectory(), "settings"));
    }

    public File getTempDirectory() {
        return validateDir(new File(getRootDirectory(), "temporary"));
    }

    public File getExecutionDirectory() {
        return validateDir(new File(getRootDirectory(), "execution"));
    }

    public String getContextName() {
        return getSolutionDescription().getName() + "-" + getEnvironmentDescription().getName();
    }
}
