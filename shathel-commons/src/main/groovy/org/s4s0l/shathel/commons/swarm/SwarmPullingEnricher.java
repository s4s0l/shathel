package org.s4s0l.shathel.commons.swarm;

import org.s4s0l.shathel.commons.core.environment.EnricherExecutable;
import org.s4s0l.shathel.commons.core.environment.EnricherExecutableParams;
import org.s4s0l.shathel.commons.core.environment.EnvironmentContext;
import org.s4s0l.shathel.commons.core.environment.ShathelNode;
import org.s4s0l.shathel.commons.core.model.ComposeFileModel;
import org.slf4j.Logger;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * @author Marcin Wielgus
 */
public class SwarmPullingEnricher extends EnricherExecutable {
    private final SwarmClusterWrapper swarmClusterWrapper;
    private static final Logger LOGGER = getLogger(SwarmPullingEnricher.class);

    public SwarmPullingEnricher(SwarmClusterWrapper swarmClusterWrapper) {
        this.swarmClusterWrapper = swarmClusterWrapper;
    }

    @Override
    protected void execute(EnricherExecutableParams params) {

        EnvironmentContext environmentContext = params.getEnvironmentContext();
        ComposeFileModel model = params.getModel();
        boolean pull = environmentContext.getEnvironmentDescription()
                .getParameterAsBoolean("pull")
                .orElse(true).booleanValue();
        EnricherExecutableParams.Provisioners provisioners = params.getProvisioners();
        if (pull) {
            model.mapImages((image) -> {
                provisioners.add("pull-image:" + image, executionContext -> {
                    for (ShathelNode nodeName : executionContext.getCurrentNodes()) {
                        swarmClusterWrapper.getDocker(nodeName).pull(image);
                    }
                });
                return image;
            });
        }
    }


}
