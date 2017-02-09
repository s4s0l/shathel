package org.s4s0l.shathel.commons.core;

import org.s4s0l.shathel.commons.core.model.ComposeFileModel;
import org.s4s0l.shathel.commons.core.stack.StackDescription;
import org.s4s0l.shathel.commons.core.stack.StackReference;
import org.s4s0l.shathel.commons.scripts.Executor;

import java.util.Collections;
import java.util.List;

/**
 * @author Matcin Wielgus
 */
public class DefaultGlobalEnricherProvider implements GlobalEnricherProvider {
    public static String versionInfo() {
        Package pkg = DefaultGlobalEnricherProvider.class.getPackage();
        String version = null;
        if (pkg != null) {
            version = pkg.getImplementationVersion();
        }
        return (version != null ? version : "Unknown Version");
    }
    @Override
    public List<Executor> getGlobalEnrichers() {
        return Collections.singletonList(context -> {
            ComposeFileModel model = (ComposeFileModel) context.get("compose");
            StackDescription stack = (StackDescription) context.get("stack");
            model.addLabelToServices("org.shathel.stack.gav", stack.getGav());
            model.addLabelToServices("org.shathel.stack.deployName", stack.getDeployName());
            model.addLabelToServices("org.shathel.stack.ga", stack.getGroup() + ":" + stack.getName());
            model.addLabelToServices("org.shathel.deployer.version", versionInfo());
            List<StackReference> dependencies = stack.getDependencies();
            int i = 0;
            for (StackReference dependency : dependencies) {
                model.addLabelToServices("org.shathel.stack.dependency." + i, dependency.getGav());
                i++;
            }
            return Collections.EMPTY_LIST;
        });
    }
}
