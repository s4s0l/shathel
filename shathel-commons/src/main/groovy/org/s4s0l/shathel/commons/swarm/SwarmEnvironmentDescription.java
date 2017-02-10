package org.s4s0l.shathel.commons.swarm;

import org.s4s0l.shathel.commons.core.environment.EnvironmentContext;

/**
 * @author Matcin Wielgus
 */
public class SwarmEnvironmentDescription {
    public static int getNodesCount(EnvironmentContext context) {
        return getManagersCount(context) + getWorkersCount(context);
    }

    public static int getManagersCount(EnvironmentContext context) {
        return context.getEnvironmentDescription()
                .getParameterAsInt("managers")
                .orElse(1);
    }

    public static int getWorkersCount(EnvironmentContext context) {
        return context.getEnvironmentDescription()
                .getParameterAsInt("workers")
                .orElse(0);
    }
}
