package org.s4s0l.shathel.commons.machine.vbox

import org.s4s0l.shathel.commons.core.Parameters
import org.s4s0l.shathel.commons.core.SolutionDescription
import org.s4s0l.shathel.commons.core.environment.Environment
import org.s4s0l.shathel.commons.core.environment.EnvironmentContext
import org.s4s0l.shathel.commons.core.environment.EnvironmentDescription
import org.s4s0l.shathel.commons.core.environment.EnvironmentProvider
import org.s4s0l.shathel.commons.core.security.SafeStorage
import org.s4s0l.shathel.commons.core.security.SafeStorageProvider
import org.s4s0l.shathel.commons.core.storage.Storage
import org.s4s0l.shathel.commons.swarm.SwarmEnvironment
import org.s4s0l.shathel.commons.swarm.SwarmNodeProvisioner
import org.s4s0l.shathel.commons.machine.MachineSwarmClusterWrapper
import org.s4s0l.shathel.commons.utils.ExtensionContext

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
        return "docker-machine-vbox"
    }

    @Override
    Environment getEnvironment(Storage s, EnvironmentDescription environmentDescription,
                               ExtensionContext ctxt, SolutionDescription solutionDescription) {
        String name = environmentDescription.getName()
        SafeStorage safeStorage = ctxt.lookupOne(SafeStorageProvider.class)
                .get().getSafeStorage(s, name)
        File rootEnvironmentDir = s.getTemporaryDirectory(name)
        EnvironmentContext context = new EnvironmentContext(environmentDescription, solutionDescription,
                safeStorage, rootEnvironmentDir)

        def machineSettingsImporterExporter = new VBoxMachineSettingsImporterExporter(context.getTempDirectory())

        MachineSwarmClusterWrapper clusterWrapper = new MachineSwarmClusterWrapper(
                context, new VBoxMachineSwarmClusterFlavour())

        return new SwarmEnvironment(context, machineSettingsImporterExporter,
                clusterWrapper, new SwarmNodeProvisioner(params, clusterWrapper))
    }
}
