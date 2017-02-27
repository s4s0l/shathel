package org.s4s0l.shathel.commons.swarm;

import org.s4s0l.shathel.commons.core.environment.EnricherExecutable;
import org.s4s0l.shathel.commons.core.environment.EnvironmentContext;
import org.s4s0l.shathel.commons.core.environment.ExecutableApiFacade;
import org.s4s0l.shathel.commons.core.model.ComposeFileModel;
import org.s4s0l.shathel.commons.core.stack.StackDescription;
import org.s4s0l.shathel.commons.scripts.Executable;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * @author Matcin Wielgus
 */
public class SwarmPullingEnricher extends EnricherExecutable {
    private final SwarmClusterWrapper swarmClusterWrapper;
    private static final Logger LOGGER = getLogger(SwarmPullingEnricher.class);

    public SwarmPullingEnricher(SwarmClusterWrapper swarmClusterWrapper) {
        this.swarmClusterWrapper = swarmClusterWrapper;
    }

    @Override
    protected List<Executable> executeProvidingProvisioner(EnvironmentContext environmentContext, ExecutableApiFacade apiFacade,
                                                           StackDescription stack, ComposeFileModel model) {
        boolean pull = environmentContext.getEnvironmentDescription()
                .getParameterAsBoolean("pull")
                .orElse(true).booleanValue();
        List<Executable> execs = new ArrayList<>();
        if (pull) {
            model.mapImages((image) -> {
                execs.add(executionContext -> {
                    for (String nodeName : swarmClusterWrapper.getNodeNames()) {
                        swarmClusterWrapper.getDocker(nodeName).pull(image);
                    }
                    return "ok";
                });
                return image;

            });
        }
        return execs;
    }


}
