package org.s4s0l.shathel.commons.swarm;

import org.s4s0l.shathel.commons.core.environment.EnricherExecutable;
import org.s4s0l.shathel.commons.core.environment.EnricherExecutableParams;
import org.s4s0l.shathel.commons.core.environment.ShathelNode;
import org.s4s0l.shathel.commons.core.model.ComposeFileModel;
import org.s4s0l.shathel.commons.docker.DockerWrapper;

/**
 * @author Marcin Wielgus
 */
public class SwarmPullingEnricher extends EnricherExecutable {

    @Override
    protected void execute(EnricherExecutableParams params) {

        ComposeFileModel model = params.getModel();
        boolean pull = params.getEnvironment().getOrDefault("SHATHEL_ENV_PULl", "true").equals("true");
        EnricherExecutableParams.Provisioners provisioners = params.getProvisioners();
        if (pull) {
            model.mapImages((image) -> {
                provisioners.add("pull-image:" + image, executionContext -> {
                    for (ShathelNode nodeName : executionContext.getCurrentNodes()) {
                        DockerWrapper docker = executionContext.getApi().getDocker(nodeName);
                        params.getEnvironmentContext().getDockerLoginInfo().ifPresent(docker::login);
                        docker.pull(image);
                    }
                });
                return image;
            });
        }
    }


}
