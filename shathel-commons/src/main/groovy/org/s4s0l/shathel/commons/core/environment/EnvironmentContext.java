package org.s4s0l.shathel.commons.core.environment;

import org.s4s0l.shathel.commons.core.SolutionDescription;
import org.s4s0l.shathel.commons.core.security.SafeStorage;

import java.io.File;

/**
 * @author Matcin Wielgus
 */
public class EnvironmentContext {
    private final EnvironmentDescription environmentDescription;
    private final SolutionDescription solutionDescription;
    private final SafeStorage safeStorage;
    private final File rootDirectory;

    public EnvironmentContext(EnvironmentDescription environmentDescription, SolutionDescription solutionDescription, SafeStorage safeStorage, File rootFile) {
        this.environmentDescription = environmentDescription;
        this.solutionDescription = solutionDescription;
        this.safeStorage = safeStorage;
        this.rootDirectory = rootFile;
    }

    public File getRootDirectory() {
        validateDir(rootDirectory);
        return rootDirectory;
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
