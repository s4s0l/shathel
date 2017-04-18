package org.s4s0l.shathel.commons.core.environment;

import org.s4s0l.shathel.commons.core.Parameters;
import org.s4s0l.shathel.commons.core.SolutionDescription;
import org.s4s0l.shathel.commons.core.security.SafeStorage;
import org.s4s0l.shathel.commons.core.storage.Storage;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

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
    public SafeStorage getSafeStorage() {
        return safeStorage;
    }

    @Override
    public EnvironmentDescription getEnvironmentDescription() {
        return environmentDescription;
    }

    @Override
    public SolutionDescription getSolutionDescription() {
        return solutionDescription;
    }

    @Override
    public File getSettingsDirectory() {
        return storage.getSettingsDirectory(getEnvironmentDescription(), getEnvironmentDescription().getName());
    }

    @Override
    public File getDataDirectory() {
        return storage.getDataDirectory(getEnvironmentDescription(), getEnvironmentDescription().getName());
    }

    @Override
    public File getTempDirectory() {
        return storage.getTemptDirectory(getEnvironmentDescription(), getEnvironmentDescription().getName());
    }

    @Override
    public File getEnrichedDirectory() {
        return storage.getEnrichedDirectory(getEnvironmentDescription(), getEnvironmentDescription().getName());
    }

    @Override
    public String getContextName() {
        return (getSolutionDescription().getName() + "-" + getEnvironmentDescription().getName()).toLowerCase();
    }

    @Override
    public File getDependencyCacheDirectory() {
        return storage.getDependencyCacheDirectory(getEnvironmentDescription(), getEnvironmentDescription().getName());
    }


    @Override
    public Map<String, String> getAsEnvironmentVariables() {
        Map<String, String> ret = new HashMap<>();
        ret.putAll(getSolutionDescription().getAsEnvironmentVariables());
        ret.putAll(getEnvironmentDescription().getAsEnvironmentVariables());
        ret.put(Parameters.parameterNameToEnvName("shathel.env.solution.name"), getContextName());
        return ret;
    }

}
