package org.s4s0l.shathel.commons.core.environment;

import org.s4s0l.shathel.commons.core.provision.EnvironmentProvisionExecutor;
import org.s4s0l.shathel.commons.core.storage.Storage;
import org.s4s0l.shathel.commons.utils.ExtensionInterface;

/**
 * @author Matcin Wielgus
 */
public interface EnvironmentProvider extends ExtensionInterface{
    String getType();

    EnvironmentProvisionExecutor getExecutor(Storage s);

    StackIntrospectionProvider getIntrospectionProvider();

    EnvironmentContainerRunner getRunner();
}
