package org.s4s0l.shathel.commons.machine.vbox;

import org.s4s0l.shathel.commons.core.Parameters;
import org.s4s0l.shathel.commons.core.SolutionDescription;
import org.s4s0l.shathel.commons.core.environment.Environment;
import org.s4s0l.shathel.commons.core.environment.EnvironmentDescription;
import org.s4s0l.shathel.commons.core.environment.EnvironmentProvider;
import org.s4s0l.shathel.commons.core.security.SafeStorage;
import org.s4s0l.shathel.commons.core.security.SafeStorageProvider;
import org.s4s0l.shathel.commons.core.storage.Storage;
import org.s4s0l.shathel.commons.core.swarm.SwarmEnvironment;
import org.s4s0l.shathel.commons.machine.MachineSettingsImporterExporter;
import org.s4s0l.shathel.commons.machine.MachineSwarmClusterWrapper;
import org.s4s0l.shathel.commons.utils.ExtensionContext;

import java.io.File;

/**
 * @author Matcin Wielgus
 */
class VBoxMachineEnvironmentProvider implements EnvironmentProvider {
    private final Parameters params;

    VBoxMachineEnvironmentProvider(Parameters params) {
        this.params = params;
    }

    @Override
    String getType() {
        return "docker-machine-vbox";
    }

    @Override
    Environment getEnvironment(Storage s, EnvironmentDescription environmentDescription,
                               ExtensionContext ctxt, SolutionDescription solutionDescription) {
        String name = environmentDescription.getName();
        SafeStorage safeStorage = ctxt.lookupOne(SafeStorageProvider.class)
                .get().getSafeStorage(s, name);
        File temporaryDirectory = s.getTemporaryDirectory(name);
        MachineSettingsImporterExporter machineSettingsImporterExporter = new VBoxMachineSettingsImporterExporter(new File(temporaryDirectory,
                "importExportTmp"));
        MachineSwarmClusterWrapper clusterWrapper = new MachineSwarmClusterWrapper(
                new File(temporaryDirectory, "clusterWrapper"),
                new VBoxMachineSwarmClusterFlavour());
        return new SwarmEnvironment(solutionDescription.getName(), temporaryDirectory,
                safeStorage, environmentDescription, machineSettingsImporterExporter,
                clusterWrapper, new VBoxNodeProvisioner(params, clusterWrapper));
    }
}
