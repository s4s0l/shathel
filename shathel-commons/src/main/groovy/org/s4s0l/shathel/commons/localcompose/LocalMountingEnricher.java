package org.s4s0l.shathel.commons.localcompose;

import org.s4s0l.shathel.commons.core.environment.EnricherExecutable;
import org.s4s0l.shathel.commons.core.environment.EnricherExecutableParams;
import org.s4s0l.shathel.commons.core.environment.EnvironmentContext;
import org.s4s0l.shathel.commons.core.environment.ExecutableApiFacade;
import org.s4s0l.shathel.commons.core.model.ComposeFileModel;
import org.s4s0l.shathel.commons.core.stack.StackDescription;
import org.s4s0l.shathel.commons.scripts.Executable;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Marcin Wielgus
 */
public class LocalMountingEnricher extends EnricherExecutable {



    @Override
    protected List<Executable> executeProvidingProvisioner(EnricherExecutableParams params) {
        ComposeFileModel model = params.getModel();
        StackDescription stack = params.getStack();
        EnvironmentContext environmentContext = params.getEnvironmentContext();
        List<Executable> execs = new ArrayList<>();
        model.mapMounts((service, volume) -> {
            if (volume.startsWith("/shathel-data/")) {
                String p = stack.getReference().getName() + "-" + service;
                p = p.toLowerCase();
                File file = new File(environmentContext.getDataDirectory(), p);
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
