package org.s4s0l.shathel.commons.dind;

import org.s4s0l.shathel.commons.core.SolutionDescription;
import org.s4s0l.shathel.commons.core.environment.Environment;
import org.s4s0l.shathel.commons.core.environment.EnvironmentDescription;
import org.s4s0l.shathel.commons.core.environment.EnvironmentProvider;
import org.s4s0l.shathel.commons.core.storage.Storage;
import org.s4s0l.shathel.commons.utils.ExtensionContext;

/**
 * @author Matcin Wielgus
 */
public class DindEnvironmentProvider implements EnvironmentProvider {


    @Override
    public String getType() {
        return "dind";
    }

    @Override
    public Environment getEnvironment(Storage s, EnvironmentDescription environmentDescription, ExtensionContext ctxt, SolutionDescription solutionDescription) {
        return null;
    }
}
