package org.s4s0l.shathel.commons.core.environment;

import org.s4s0l.shathel.commons.core.provision.EnvironmentProvisionExecutor;

/**
 * @author Matcin Wielgus
 */
public class Environment {
    private final StackIntrospectionProvider introspectionProvider;
    private final EnvironmentProvisionExecutor provisionExecutor;
    private final EnvironmentContainerRunner containerRunner;


    public Environment(StackIntrospectionProvider introspectionProvider,
                       EnvironmentProvisionExecutor provisionExecutor,
                       EnvironmentContainerRunner containerRunner) {
        this.introspectionProvider = introspectionProvider;
        this.provisionExecutor = provisionExecutor;
        this.containerRunner = containerRunner;
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

}
