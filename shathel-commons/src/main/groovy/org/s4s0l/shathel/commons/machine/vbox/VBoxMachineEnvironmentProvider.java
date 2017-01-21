package org.s4s0l.shathel.commons.machine.vbox;

import org.s4s0l.shathel.commons.core.Parameters;
import org.s4s0l.shathel.commons.core.SolutionDescription;
import org.s4s0l.shathel.commons.core.environment.Environment;
import org.s4s0l.shathel.commons.core.environment.EnvironmentDescription;
import org.s4s0l.shathel.commons.core.environment.EnvironmentProvider;
import org.s4s0l.shathel.commons.core.security.SafeStorage;
import org.s4s0l.shathel.commons.core.security.SafeStorageProvider;
import org.s4s0l.shathel.commons.core.storage.Storage;
import org.s4s0l.shathel.commons.machine.MachineEnvironment;
import org.s4s0l.shathel.commons.utils.ExtensionContext;

/**
 * @author Matcin Wielgus
 */
public class VBoxMachineEnvironmentProvider implements EnvironmentProvider {
    private final Parameters params;

    public VBoxMachineEnvironmentProvider(Parameters params) {
        this.params = params;
    }

    @Override
    public String getType() {
        return "docker-machine-vbox";
    }

    @Override
    public Environment getEnvironment(Storage s, EnvironmentDescription environmentDescription,
                                      ExtensionContext ctxt, SolutionDescription solutionDescription) {
        String name = environmentDescription.getName();
        SafeStorage safeStorage = ctxt.lookupOne(SafeStorageProvider.class)
                .get().getSafeStorage(s, name);
        return new MachineEnvironment(solutionDescription.getName(), s.getTemporaryDirectory(name),
                safeStorage, environmentDescription, new VBoxMachineProvisioner(params));
    }
}
