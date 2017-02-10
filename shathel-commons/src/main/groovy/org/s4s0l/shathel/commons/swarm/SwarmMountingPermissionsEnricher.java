package org.s4s0l.shathel.commons.swarm;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.s4s0l.shathel.commons.core.environment.EnricherExecutor;
import org.s4s0l.shathel.commons.core.environment.EnvironmentApiFacade;
import org.s4s0l.shathel.commons.core.environment.EnvironmentContext;
import org.s4s0l.shathel.commons.core.model.ComposeFileModel;
import org.s4s0l.shathel.commons.core.stack.StackDescription;
import org.s4s0l.shathel.commons.scripts.Executor;
import org.slf4j.Logger;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * @author Matcin Wielgus
 */
public class SwarmMountingPermissionsEnricher extends EnricherExecutor {
    private final SwarmClusterWrapper swarmClusterWrapper;
    private static final Logger LOGGER = getLogger(SwarmMountingPermissionsEnricher.class);

    public SwarmMountingPermissionsEnricher(SwarmClusterWrapper swarmClusterWrapper) {
        this.swarmClusterWrapper = swarmClusterWrapper;
    }

    @Override
    protected List<Executor> executeProvidingProvisioner(EnvironmentContext environmentContext, EnvironmentApiFacade apiFacade,
                           StackDescription stack, ComposeFileModel model) {
        List<Executor> execs = new ArrayList<>();
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
