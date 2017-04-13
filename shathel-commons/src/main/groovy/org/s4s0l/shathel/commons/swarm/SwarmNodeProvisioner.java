package org.s4s0l.shathel.commons.swarm;

import org.s4s0l.shathel.commons.core.environment.EnvironmentContext;

import java.io.File;

/**
 * @author Marcin Wielgus
 */
@Deprecated
public class SwarmNodeProvisioner implements NodeProvisioner {
    public SwarmNodeProvisioner(SwarmClusterWrapper clusterWrapper, SwarmNodeCreator swarmNodeCreator) {
        this.clusterWrapper = clusterWrapper;
        this.swarmNodeCreator = swarmNodeCreator;
    }

    @Override
    public boolean createMachines(File workDir, EnvironmentContext context) {
        String net = context.getEnvironmentDescription().getParameter("net").orElse("42.42.42");
        return new SwarmClusterCreator(clusterWrapper, swarmNodeCreator,
                workDir, context.getContextName(),
                context.getEnvironmentDescription().getManagersCount(),
                context.getEnvironmentDescription().getWorkersCount(), net)
                .createMachines();
    }

    private final SwarmClusterWrapper clusterWrapper;
    private final SwarmNodeCreator swarmNodeCreator;
}
