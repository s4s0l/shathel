package org.s4s0l.shathel.commons.core;

import org.s4s0l.shathel.commons.files.model.SolutionFileModel;

/**
 * @author Matcin Wielgus
 */
public class SolutionDescription {
    private final SolutionFileModel model;

    public SolutionDescription(SolutionFileModel model) {
        this.model = model;
    }

    EnvironmentDescription getEnvironmentDescription(String envName){
        return null;
    }

}
