package org.s4s0l.shathel.commons.localcompose;

import org.s4s0l.shathel.commons.core.SolutionDescription;
import org.s4s0l.shathel.commons.core.environment.*;
import org.s4s0l.shathel.commons.core.security.LazyInitiableSafeStorage;
import org.s4s0l.shathel.commons.core.security.SafeStorageProvider;
import org.s4s0l.shathel.commons.core.storage.Storage;
import org.s4s0l.shathel.commons.utils.ExtensionContext;

/**
 * @author Matcin Wielgus
 */
public class LocaEnvironmentProvider implements EnvironmentProvider {


    @Override
    public String getType() {
        return "docker-compose";
    }

    @Override
    public Environment getEnvironment(EnvironmentContext environmentContext) {

        return new LocalEnvironment(environmentContext);
    }


}
