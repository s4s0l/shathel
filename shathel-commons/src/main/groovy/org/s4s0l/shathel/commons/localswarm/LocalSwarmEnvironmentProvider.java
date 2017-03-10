package org.s4s0l.shathel.commons.localswarm;

import org.s4s0l.shathel.commons.core.environment.Environment;
import org.s4s0l.shathel.commons.core.environment.EnvironmentContext;
import org.s4s0l.shathel.commons.core.environment.EnvironmentProvider;
import org.s4s0l.shathel.commons.localcompose.LocalEnvironment;

/**
 * @author Marcin Wielgus
 */
public class LocalSwarmEnvironmentProvider implements EnvironmentProvider {


    @Override
    public String getType() {
        return "local-swarm";
    }

    @Override
    public Environment getEnvironment(EnvironmentContext environmentContext) {

        return new LocalSwarmEnvironment(environmentContext);
    }


}