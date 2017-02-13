package org.s4s0l.shathel.commons.core;

import org.s4s0l.shathel.commons.core.environment.EnricherExecutor;
import org.s4s0l.shathel.commons.core.environment.EnvironmentApiFacade;
import org.s4s0l.shathel.commons.core.environment.EnvironmentContext;
import org.s4s0l.shathel.commons.core.model.ComposeFileModel;
import org.s4s0l.shathel.commons.core.stack.StackDescription;
import org.s4s0l.shathel.commons.core.stack.StackReference;
import org.s4s0l.shathel.commons.scripts.Executor;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author Matcin Wielgus
 */
public class DefaultGlobalEnricherProvider implements GlobalEnricherProvider {


    @Override
    public List<Executor> getGlobalEnrichers() {
        return Arrays.asList(
                new LabelingEnricher(),
                new VariablesEnricher());


    }

    private static class LabelingEnricher extends EnricherExecutor {
        @Override
        protected void execute(EnvironmentContext environmentContext, EnvironmentApiFacade apiFacade, StackDescription stack, ComposeFileModel model) {
            model.addLabelToServices("org.shathel.stack.gav", stack.getGav());
            model.addLabelToServices("org.shathel.stack.deployName", stack.getDeployName());
            model.addLabelToServices("org.shathel.stack.ga", stack.getGroup() + ":" + stack.getName());
            model.addLabelToServices("org.shathel.stack.marker", "true");
            model.addLabelToServices("org.shathel.deployer.version", versionInfo());
            List<StackReference> dependencies = stack.getDependencies();
            int i = 0;
            for (StackReference dependency : dependencies) {
                model.addLabelToServices("org.shathel.stack.dependency." + i, dependency.getGav());
                i++;
            }
        }

        public static String versionInfo() {
            Package pkg = DefaultGlobalEnricherProvider.class.getPackage();
            String version = null;
            if (pkg != null) {
                version = pkg.getImplementationVersion();
            }
            return (version != null ? version : "Unknown Version");
        }
    }

    private static class VariablesEnricher extends EnricherExecutor {
        @Override
        protected void execute(EnvironmentContext environmentContext, EnvironmentApiFacade apiFacade,
                               StackDescription stack, ComposeFileModel model) {
            int size = apiFacade.getExpectedNodeCount();
            int quorum = (int) Math.floor(size / 2) + 1;
            model.replaceInAllStrings("${SHATHEL_ENV_SIZE}", "" + size);
            model.replaceInAllStrings("${SHATHEL_ENV_QUORUM}", "" + quorum);
            model.replaceInAllStrings("$SHATHEL_ENV_SIZE", "" + size);
            model.replaceInAllStrings("$SHATHEL_ENV_QUORUM", "" + quorum);

            int msize = apiFacade.getExpectedManagerNodeCount();
            int mquorum = (int) Math.floor(msize / 2) + 1;

            model.replaceInAllStrings("${SHATHEL_ENV_MGM_SIZE}", "" + msize);
            model.replaceInAllStrings("${SHATHEL_ENV_MGM_QUORUM}", "" + mquorum);
            model.replaceInAllStrings("$SHATHEL_ENV_MGM_SIZE", "" + msize);
            model.replaceInAllStrings("$SHATHEL_ENV_MGM_QUORUM", "" + mquorum);

        }
    }


}
