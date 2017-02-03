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
        String net = parameters.getParameter("shathel.network." + context.getEnvironmentDescription().name + ".net").orElse("20.20.20")

        return new SwarmClusterCreator(clusterWrapper, workDir,
                context.contextName,
                getManagersCount(context),
                getWorkersCount(context), net).createMachines();
    }

    private int getManagersCount(EnvironmentContext context) {
        return context.getEnvironmentDescription()
                .getParameterAsInt("managers")
                .orElse(1);
    }

    private int getWorkersCount(EnvironmentContext context) {
        return context.getEnvironmentDescription()
                .getParameterAsInt("workers")
                .orElse(0);
    }


}
