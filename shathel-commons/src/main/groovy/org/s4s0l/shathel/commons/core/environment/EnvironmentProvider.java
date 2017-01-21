package org.s4s0l.shathel.commons.core.environment;

import org.s4s0l.shathel.commons.core.SolutionDescription;
import org.s4s0l.shathel.commons.core.provision.EnvironmentProvisionExecutor;
import org.s4s0l.shathel.commons.core.storage.Storage;
import org.s4s0l.shathel.commons.utils.ExtensionContext;
import org.s4s0l.shathel.commons.utils.ExtensionInterface;

/**
 *
 * TODO tu ma być getEnvironment(EnvDesc)
 * @author Matcin Wielgus
 */
public interface EnvironmentProvider extends ExtensionInterface{
    String getType();

    Environment getEnvironment(Storage s, EnvironmentDescription environmentDescription,
                               ExtensionContext ctxt,SolutionDescription solutionDescription);

}
