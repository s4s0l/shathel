package org.s4s0l.shathel.commons.localcompose;

import org.s4s0l.shathel.commons.core.environment.EnvironmentContainerRunner;
import org.s4s0l.shathel.commons.core.environment.EnvironmentProvider;
import org.s4s0l.shathel.commons.core.environment.StackIntrospectionProvider;
import org.s4s0l.shathel.commons.core.provision.EnvironmentProvisionExecutor;
import org.s4s0l.shathel.commons.core.storage.Storage;
import org.s4s0l.shathel.commons.provisions.LocalProvisioner;

import java.io.File;

/**
 * @author Matcin Wielgus
 */
public class LocalComposeEnvironmentProvider implements EnvironmentProvider {

    @Override
    public String getType() {
        return "LOCAL_COMPOSE";
    }

    private final LocalCompose localCompose = new LocalCompose();


    @Override
    public EnvironmentProvisionExecutor getExecutor(Storage s) {
        File mounts = s.getMountsDir();
        mounts.mkdirs();
        return new LocalProvisioner(mounts);
    }

    @Override
    public StackIntrospectionProvider getIntrospectionProvider() {
        return localCompose;
    }

    @Override
    public EnvironmentContainerRunner getRunner() {
        return localCompose;
    }


}
