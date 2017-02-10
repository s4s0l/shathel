package org.s4s0l.shathel.commons.swarm

import org.s4s0l.shathel.commons.core.Parameters
import org.s4s0l.shathel.commons.core.environment.EnvironmentContext

/**
 * @author Matcin Wielgus
 */
class SwarmNodeProvisioner implements NodeProvisioner {
    private final Parameters parameters;
    private final SwarmClusterWrapper clusterWrapper;

    SwarmNodeProvisioner(Parameters parameters, SwarmClusterWrapper clusterWrapper) {
        this.parameters = parameters;
        this.clusterWrapper = clusterWrapper;
    }

    @Override
    boolean createMachines(File workDir, EnvironmentContext context) {
        String net = context.getEnvironmentDescription().getParameter("net").orElse("42.42.42")

        return new SwarmClusterCreator(clusterWrapper, workDir,
                context.contextName,
                SwarmEnvironmentDescription.getManagersCount(context),
                SwarmEnvironmentDescription.getWorkersCount(context), net).createMachines();
    }




}
