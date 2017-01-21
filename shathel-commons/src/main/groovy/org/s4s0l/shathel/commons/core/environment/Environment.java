package org.s4s0l.shathel.commons.core.environment;

import org.s4s0l.shathel.commons.core.provision.EnvironmentProvisionExecutor;
import org.s4s0l.shathel.commons.core.security.SafeStorage;
import org.s4s0l.shathel.commons.core.storage.Storage;

import java.io.File;

/**
 *
 * @author Matcin Wielgus
 */
public interface Environment {

    File getExecutionDirectory();

    boolean isInitialized();

    void initialize();

    void start();

    boolean isStarted();

    void stop();

    void destroy();

    void verify();

    StackIntrospectionProvider getIntrospectionProvider();

    EnvironmentProvisionExecutor getProvisionExecutor();

    EnvironmentContainerRunner getContainerRunner();


}
