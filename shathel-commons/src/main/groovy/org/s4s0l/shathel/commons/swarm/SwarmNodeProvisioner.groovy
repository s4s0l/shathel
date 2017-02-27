package org.s4s0l.shathel.commons.swarm

import org.s4s0l.shathel.commons.core.environment.EnvironmentContext

/**
 * @author Matcin Wielgus
 */
class SwarmNodeProvisioner implements NodeProvisioner {
    private final SwarmClusterWrapper clusterWrapper;

    SwarmNodeProvisioner( SwarmClusterWrapper clusterWrapper) {
        this.clusterWrapper = clusterWrapper;
    }

    @Override
    boolean createMachines(File workDir, EnvironmentContext context) {
        String net = context.getEnvironmentDescription().getParameter("net").orElse("42.42.42")

        return new SwarmClusterCreator(clusterWrapper, workDir,
                context.contextName,
                context.environmentDescription.managersCount,
                context.environmentDescription.workersCount, net).createMachines();
    }




}
