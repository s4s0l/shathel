package org.s4s0l.shathel.commons.core.environment;

import org.s4s0l.shathel.commons.core.DockerLoginInfo;
import org.s4s0l.shathel.commons.core.Parameters;
import org.s4s0l.shathel.commons.core.SolutionDescription;
import org.s4s0l.shathel.commons.core.security.SafeStorage;
import org.s4s0l.shathel.commons.core.storage.Storage;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * @author Marcin Wielgus
 */
public class EnvironmentContextImpl implements EnvironmentContext {

    private final EnvironmentDescription environmentDescription;
    private final SolutionDescription solutionDescription;
    private final SafeStorage safeStorage;
    private final Storage storage;

    public EnvironmentContextImpl(EnvironmentDescription environmentDescription,
                                  SolutionDescription solutionDescription, SafeStorage safeStorage, Storage storage) {
        this.environmentDescription = environmentDescription;
        this.solutionDescription = solutionDescription;
        this.safeStorage = safeStorage;
        this.storage = storage;
    }

    @Override
    public File getSafeDirectory() {
        return storage.getSafeDirectory(environmentDescription.getParameters(), environmentDescription.getEnvironmentName());
    }

    @Override
    public String getEnvironmentName() {
        return environmentDescription.getEnvironmentName();
    }

    @Override
    public SafeStorage getSafeStorage() {
        return safeStorage;
    }

    private EnvironmentDescription getEnvironmentDescription() {
        return environmentDescription;
    }

    private SolutionDescription getSolutionDescription() {
        return solutionDescription;
    }

    @Override
    public File getAnsibleInventoryFile() {
        return new File(getSettingsDirectory(), "ansible-inventory");
    }

    @Override
    public File getSettingsDirectory() {
        return storage.getSettingsDirectory(getEnvironmentDescription().getParameters(), getEnvironmentDescription().getEnvironmentName());
    }

    @Override
    public File getDataDirectory() {
        return storage.getDataDirectory(getEnvironmentDescription().getParameters(), getEnvironmentDescription().getEnvironmentName());
    }

    @Override
    public File getTempDirectory() {
        return storage.getTemptDirectory(getEnvironmentDescription().getParameters(), getEnvironmentDescription().getEnvironmentName());
    }

    @Override
    public File getEnrichedDirectory() {
        return storage.getEnrichedDirectory(getEnvironmentDescription().getParameters(), getEnvironmentDescription().getEnvironmentName());
    }

    @Override
    public String getContextName() {
        return (getSolutionDescription().getSolutionName() + "-" + getEnvironmentDescription().getEnvironmentName()).toLowerCase();
    }

    @Override
    public File getCertsDirectory() {
        return ensureExists(new File(getSettingsDirectory(), "certs"));
    }

    @Override
    public Optional<DockerLoginInfo> getDockerLoginInfo() {
        return solutionDescription.getDockerLoginInfo();
    }

    @Override
    public File getDependencyCacheDirectory() {
        return storage.getDependencyCacheDirectory(getEnvironmentDescription().getParameters());
    }


    @Override
    public Map<String, String> getAsEnvironmentVariables() {
        Map<String, String> tmp = new HashMap<>();
        tmp.putAll(getSolutionDescription().getAsEnvironmentVariables());
        tmp.putAll(getEnvironmentDescription().getAsEnvironmentVariables());
        tmp.put(Parameters.parameterNameToEnvName("shathel.env.solution.name"), getContextName());
        return safeStorage.fixValues(tmp);
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private File ensureExists(File f) {
        f.mkdirs();
        return f;
    }

}
