package org.s4s0l.shathel.commons.swarm;

import org.s4s0l.shathel.commons.core.environment.EnricherExecutable;
import org.s4s0l.shathel.commons.core.environment.EnricherExecutableParams;
import org.s4s0l.shathel.commons.core.environment.ShathelNode;
import org.s4s0l.shathel.commons.core.model.ComposeFileModel;
import org.slf4j.Logger;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * @author Marcin Wielgus
 */
public class SwarmMountingPermissionsEnricher extends EnricherExecutable {
    private final SwarmClusterWrapper swarmClusterWrapper;
    private static final Logger LOGGER = getLogger(SwarmMountingPermissionsEnricher.class);

    public SwarmMountingPermissionsEnricher(SwarmClusterWrapper swarmClusterWrapper) {
        this.swarmClusterWrapper = swarmClusterWrapper;
    }

    @Override
    protected void execute(EnricherExecutableParams params) {
        ComposeFileModel model = params.getModel();
        EnricherExecutableParams.Provisioners provisioners = params.getProvisioners();
        model.mapMounts((service, volume) -> {
            if (volume.startsWith("/shathel-data/")) {
                String[] split = volume.split(":");
                String localPart = split[0].replace("/shathel-data", swarmClusterWrapper.getDataDirectory());
                LOGGER.debug("Changing path {} to be owned by 1000", localPart);
                provisioners.add("prepare-mount-dir:" + localPart, context -> {
                    for (ShathelNode nodeName : context.getCurrentNodes()) {
                        swarmClusterWrapper.sudo(nodeName.getNodeName(), "mkdir -p " + localPart);
                        swarmClusterWrapper.sudo(nodeName.getNodeName(), "chown -R 1000 " + localPart);
                    }
                });
                return localPart + ":" + split[1];

            } else {
                return volume;
            }
        });
    }


}
