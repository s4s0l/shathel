package org.s4s0l.shathel.commons.localcompose;

import org.s4s0l.shathel.commons.core.environment.EnricherExecutor;
import org.s4s0l.shathel.commons.core.environment.EnvironmentApiFacade;
import org.s4s0l.shathel.commons.core.environment.EnvironmentContext;
import org.s4s0l.shathel.commons.core.model.ComposeFileModel;
import org.s4s0l.shathel.commons.core.stack.StackDescription;
import org.s4s0l.shathel.commons.scripts.Executor;
import org.s4s0l.shathel.commons.swarm.SwarmClusterWrapper;
import org.slf4j.Logger;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * @author Matcin Wielgus
 */
public class LocalMountingEnricher extends EnricherExecutor {



    @Override
    protected List<Executor> executeProvidingProvisioner(EnvironmentContext environmentContext, EnvironmentApiFacade apiFacade,
                                                         StackDescription stack, ComposeFileModel model) {
        List<Executor> execs = new ArrayList<>();
        model.mapMounts((service, volume) -> {
            if (volume.startsWith("/shathel-data/")) {
                String p = stack.getReference().getName() + "-" + service;
                p = p.toLowerCase();
                File file = new File(environmentContext.getWorkDirectory(), p);
                String absolutePath = file.getAbsolutePath();
                execs.add(context -> {
                    file.mkdirs();
                    return "ok";
                });
                return volume.replace("/shathel-data", absolutePath);
            } else {
                return volume;
            }
        });
        return execs;
    }


}
