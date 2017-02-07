package org.s4s0l.shathel.commons.core;

import org.s4s0l.shathel.commons.core.environment.EnvironmentDescription;
import org.s4s0l.shathel.commons.core.model.SolutionFileModel;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * @author Matcin Wielgus
 */
public class SolutionDescription {
    private final Parameters overrides;
    private final SolutionFileModel model;
    private final Map<String, String> parameters = new HashMap<>();

    public SolutionDescription(Parameters overrides, SolutionFileModel model) {
        this.overrides = overrides;
        this.model = model;
    }

    public EnvironmentDescription getEnvironmentDescription(String envName) {
        Map<String, String> x = model.getEnvironment(envName);
        return new EnvironmentDescription(overrides, x.get("name"), x.get("type"), x);
    }

    public String getName() {
        return model.getName();
    }

    public Optional<String> getParameter(String name) {
        String s = overrides.getParameter("shathel.solution." + name).orElseGet(() -> parameters.get(name));
        return Optional.ofNullable(s);
    }
}
