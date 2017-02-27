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
public class SwarmMountingPermissionsEnricher extends EnricherExecutable {
    private final SwarmClusterWrapper swarmClusterWrapper;
    private static final Logger LOGGER = getLogger(SwarmMountingPermissionsEnricher.class);

    public SwarmMountingPermissionsEnricher(SwarmClusterWrapper swarmClusterWrapper) {
        this.swarmClusterWrapper = swarmClusterWrapper;
    }

    @Override
    protected List<Executable> executeProvidingProvisioner(EnvironmentContext environmentContext, ExecutableApiFacade apiFacade,
                                                           StackDescription stack, ComposeFileModel model) {
        List<Executable> execs = new ArrayList<>();
        model.mapMounts((service, volume) -> {
            if (volume.startsWith("/shathel-data/")) {
                String[] split = volume.split(":");
                String localPart = split[0].replace("/shathel-data", swarmClusterWrapper.getDataDirectory());
                LOGGER.debug("Changing path {} to be owned by 1000", localPart);
                execs.add(context -> {
                    for (String nodeName : swarmClusterWrapper.getNodeNames()) {
                        swarmClusterWrapper.sudo(nodeName, "mkdir -p " + localPart);
                        swarmClusterWrapper.sudo(nodeName, "chown -R 1000 " + localPart);
                    }
                    return "ok";
                });
                return localPart + ":" + split[1];

            } else {
                return volume;
            }
        });
        return execs;
    }


}
