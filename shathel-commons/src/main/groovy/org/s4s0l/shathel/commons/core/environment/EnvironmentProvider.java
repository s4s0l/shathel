package org.s4s0l.shathel.commons.core.environment;

import org.s4s0l.shathel.commons.utils.ExtensionInterface;

/**
 * TODO tu ma być getEnvironment(EnvDesc)
 *
 * @author Marcin Wielgus
 */
public interface EnvironmentProvider extends ExtensionInterface {
    String getType();

    Environment getEnvironment(EnvironmentDescription environmentDescription, EnvironmentContext environmentContext);

}
