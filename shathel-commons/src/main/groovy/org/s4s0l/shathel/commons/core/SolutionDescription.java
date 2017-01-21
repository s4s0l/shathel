package org.s4s0l.shathel.commons.core;

import org.s4s0l.shathel.commons.core.environment.EnvironmentDescription;
import org.s4s0l.shathel.commons.core.model.SolutionFileModel;

import java.util.Map;

/**
 * @author Matcin Wielgus
 */
public class SolutionDescription {
    private final SolutionFileModel model;

    public SolutionDescription(SolutionFileModel model) {
        this.model = model;
    }

    public EnvironmentDescription getEnvironmentDescription(String envName) {
        Map<String, String> x = model.getEnvironment(envName);
        return new EnvironmentDescription(x.get("type"), x.get("type"), x);
    }

    public String getName() {
        return model.getName();
    }
}
