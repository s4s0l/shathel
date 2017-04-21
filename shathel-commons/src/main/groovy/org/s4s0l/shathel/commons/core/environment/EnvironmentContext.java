package org.s4s0l.shathel.commons.core.environment;

import org.s4s0l.shathel.commons.core.SolutionDescription;
import org.s4s0l.shathel.commons.core.security.SafeStorage;

import java.io.File;
import java.util.Map;

/**
 * @author Marcin Wielgus
 */
public interface EnvironmentContext {
    SafeStorage getSafeStorage();

    EnvironmentDescription getEnvironmentDescription();

    SolutionDescription getSolutionDescription();

    File getCertsDirectory();

    File getSettingsDirectory();

    File getDataDirectory();

    File getTempDirectory();

    File getEnrichedDirectory();

    File getAnsibleInventoryFile();

    File getDependencyCacheDirectory();

    String getContextName();

    Map<String, String> getAsEnvironmentVariables();

}
