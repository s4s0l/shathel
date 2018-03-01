package org.s4s0l.shathel.commons.core.environment;

import org.s4s0l.shathel.commons.core.DockerLoginInfo;
import org.s4s0l.shathel.commons.core.EnvironmentVariabllesContainer;
import org.s4s0l.shathel.commons.core.security.SafeStorage;

import java.io.File;
import java.util.Optional;

/**
 * @author Marcin Wielgus
 */
public interface EnvironmentContext extends EnvironmentVariabllesContainer {

    String getEnvironmentName();

    SafeStorage getSafeStorage();

    File getCertsDirectory();

    File getSettingsDirectory();

    File getDataDirectory();

    File getTempDirectory();

    File getEnrichedDirectory();

    File getSafeDirectory();

    File getAnsibleInventoryFile();

    File getDependencyCacheDirectory();

    String getContextName();

    Optional<DockerLoginInfo> getDockerLoginInfo();

}
