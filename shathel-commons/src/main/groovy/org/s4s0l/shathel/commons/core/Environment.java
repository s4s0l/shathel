package org.s4s0l.shathel.commons.core;

/**
 * @author Matcin Wielgus
 */
public class Environment {
    private final StackIntrospectionProvider introspectionProvider;
    private final EnvironmentProvisionExecutor provisionExecutor;
    private final EnvironmentContainerRunner containerRunner;
    private final Storage storage;

    public Environment(StackIntrospectionProvider introspectionProvider,
                       EnvironmentProvisionExecutor provisionExecutor,
                       EnvironmentContainerRunner containerRunner,
                       Storage storage) {
        this.introspectionProvider = introspectionProvider;
        this.provisionExecutor = provisionExecutor;
        this.containerRunner = containerRunner;
        this.storage = storage;
    }

    public StackIntrospectionProvider getIntrospectionProvider() {
        return introspectionProvider;
    }

    public EnvironmentProvisionExecutor getProvisionExecutor() {
        return provisionExecutor;
    }

    public EnvironmentContainerRunner getContainerRunner() {
        return containerRunner;
    }

    public Storage getStorage() {
        return storage;
    }
}
