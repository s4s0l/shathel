package org.s4s0l.shathel.commons.localcompose;

import org.s4s0l.shathel.commons.core.environment.Environment;
import org.s4s0l.shathel.commons.core.environment.EnvironmentContext;
import org.s4s0l.shathel.commons.core.environment.EnvironmentProvider;

/**
 * @author Matcin Wielgus
 */
public class LocalEnvironmentProvider implements EnvironmentProvider {


    @Override
    public String getType() {
        return "docker-compose";
    }

    @Override
    public Environment getEnvironment(EnvironmentContext environmentContext) {

        return new LocalEnvironment(environmentContext);
    }


}
