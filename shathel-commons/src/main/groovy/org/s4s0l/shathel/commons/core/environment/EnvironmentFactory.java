package org.s4s0l.shathel.commons.core.environment;

import org.s4s0l.shathel.commons.core.provision.EnvironmentProvisionExecutor;
import org.s4s0l.shathel.commons.core.storage.Storage;
import org.s4s0l.shathel.commons.utils.ExtensionContext;

/**
 * @author Matcin Wielgus
 */
public class EnvironmentFactory {
    private final ExtensionContext extensions;

    public EnvironmentFactory(ExtensionContext extensions) {
        this.extensions = extensions;
    }


    public Environment getEnvironment(EnvironmentDescription environmentDescription, Storage s){
        String type = environmentDescription.getType();
        EnvironmentProvider environmentProvider = extensions.lookupOneMatching(EnvironmentProvider.class, x -> x.getType().equals(type)).get();

        EnvironmentProvisionExecutor executor = environmentProvider.getExecutor(s);
        StackIntrospectionProvider introspectionProvider = environmentProvider.getIntrospectionProvider();
        EnvironmentContainerRunner containerRunner = environmentProvider.getRunner();

        return new Environment(introspectionProvider, executor, containerRunner);
    }
}
