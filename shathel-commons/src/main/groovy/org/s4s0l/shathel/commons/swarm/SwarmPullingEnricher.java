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
    private static final Logger LOGGER = getLogger(SwarmPullingEnricher.class);


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
                        executionContext.getApiFacade().getDocker(nodeName).pull(image);
                    }
                });
                return image;
            });
        }
    }


}
