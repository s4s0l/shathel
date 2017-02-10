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
    Environment getEnvironment(EnvironmentContext environmentContext) {

        def machineSettingsImporterExporter = new VBoxMachineSettingsImporterExporter(environmentContext.getTempDirectory())

        MachineSwarmClusterWrapper clusterWrapper = new MachineSwarmClusterWrapper(
                environmentContext, new VBoxMachineSwarmClusterFlavour())

        return new SwarmEnvironment(environmentContext, machineSettingsImporterExporter,
                clusterWrapper, new SwarmNodeProvisioner(params, clusterWrapper))
    }
}
