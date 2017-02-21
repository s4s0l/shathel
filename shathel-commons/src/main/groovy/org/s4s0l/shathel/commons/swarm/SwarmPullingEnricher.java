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
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * @author Matcin Wielgus
 */
public class SwarmPullingEnricher extends EnricherExecutor {
    private final SwarmClusterWrapper swarmClusterWrapper;
    private static final Logger LOGGER = getLogger(SwarmPullingEnricher.class);

    public SwarmPullingEnricher(SwarmClusterWrapper swarmClusterWrapper) {
        this.swarmClusterWrapper = swarmClusterWrapper;
    }

    @Override
    protected List<Executor> executeProvidingProvisioner(EnvironmentContext environmentContext, EnvironmentApiFacade apiFacade,
                                                         StackDescription stack, ComposeFileModel model) {
        boolean pull = environmentContext.getEnvironmentDescription()
                .getParameterAsBoolean("pull")
                .orElse(true).booleanValue();
        List<Executor> execs = new ArrayList<>();
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
