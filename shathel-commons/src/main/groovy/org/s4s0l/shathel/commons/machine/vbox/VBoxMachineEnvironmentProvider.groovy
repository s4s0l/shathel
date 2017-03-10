package org.s4s0l.shathel.commons.machine.vbox

import org.s4s0l.shathel.commons.core.Parameters
import org.s4s0l.shathel.commons.core.environment.Environment
import org.s4s0l.shathel.commons.core.environment.EnvironmentContext
import org.s4s0l.shathel.commons.core.environment.EnvironmentProvider
import org.s4s0l.shathel.commons.machine.MachineSwarmClusterWrapper
import org.s4s0l.shathel.commons.swarm.SwarmEnvironment
import org.s4s0l.shathel.commons.swarm.SwarmNodeProvisioner

/**
 * @author Marcin Wielgus
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


        MachineSwarmClusterWrapper clusterWrapper = new MachineSwarmClusterWrapper(
                environmentContext)

        VBoxMachineNodeCreator nodeCreator = new VBoxMachineNodeCreator(clusterWrapper.getWrapper(), environmentContext)
        def machineSettingsImporterExporter = new VBoxMachineSettingsImporterExporter(environmentContext.getTempDirectory(), clusterWrapper.getWrapper())

        return new SwarmEnvironment(environmentContext, machineSettingsImporterExporter,
                clusterWrapper, new SwarmNodeProvisioner( clusterWrapper,nodeCreator))
    }
}
