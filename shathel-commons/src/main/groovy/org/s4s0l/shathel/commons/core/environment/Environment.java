package org.s4s0l.shathel.commons.core.environment;

import org.s4s0l.shathel.commons.core.provision.EnvironmentProvisionExecutor;
import org.s4s0l.shathel.commons.core.security.SafeStorage;
import org.s4s0l.shathel.commons.core.storage.Storage;
import org.s4s0l.shathel.commons.scripts.Executor;

import java.io.File;
import java.util.List;

/**
 * @author Matcin Wielgus
 */
public interface Environment {

    File getExecutionDirectory();

    /**
     * Checks if amount of existing nodes fulfills environments
     * requirements. These nodes don't need to be started.
     * Just need to exist.
     *
     * @return see abbove
     */
    boolean isInitialized();

    /**
     * Starts all nodes.
     * Creates missing nodes.
     */
    void initialize();

    /**
     * Starts all nodes known to this environment
     */
    void start();

    /**
     * Are all known nodes started.
     *
     * @return see above
     */
    boolean isStarted();

    void stop();

    void destroy();

    /**
     * Verifies that environment is healthy and ready to
     * to accept stacks operations. Example:
     * at least one node is manager, and is reachable.
     * All running nodes come from the same cluster
     * and all are able to see each other. So
     * it is possible that {@link #isInitialized()} says false
     * while verification can pass.
     */
    void verify();

    void save();

    void load();


    StackIntrospectionProvider getIntrospectionProvider();

    EnvironmentProvisionExecutor getProvisionExecutor();

    EnvironmentContainerRunner getContainerRunner();

    EnvironmentContext getEnvironmentContext();

    EnvironmentApiFacade getEnvironmentApiFacade();

    List<Executor> getEnvironmentEnrichers();

}
