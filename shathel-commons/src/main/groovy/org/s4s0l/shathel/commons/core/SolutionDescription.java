package org.s4s0l.shathel.commons.core;

import org.s4s0l.shathel.commons.core.environment.EnvironmentDescription;
import org.s4s0l.shathel.commons.core.model.SolutionFileModel;

/**
 * @author Matcin Wielgus
 */
public class SolutionDescription {
    private final SolutionFileModel model;

    public SolutionDescription(SolutionFileModel model) {
        this.model = model;
    }

    EnvironmentDescription getEnvironmentDescription(String envName) {
        return new EnvironmentDescription(envName, "LOCAL_COMPOSE");
    }

}
