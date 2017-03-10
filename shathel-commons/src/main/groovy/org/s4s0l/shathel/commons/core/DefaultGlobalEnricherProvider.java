package org.s4s0l.shathel.commons.core;

import org.s4s0l.shathel.commons.core.environment.*;
import org.s4s0l.shathel.commons.core.model.ComposeFileModel;
import org.s4s0l.shathel.commons.core.stack.StackDependency;
import org.s4s0l.shathel.commons.core.stack.StackDescription;
import org.s4s0l.shathel.commons.core.stack.StackReference;
import org.s4s0l.shathel.commons.scripts.Executable;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * @author Marcin Wielgus
 */
public class DefaultGlobalEnricherProvider implements GlobalEnricherProvider {


    @Override
    public List<Executable> getGlobalEnrichers() {
        return Arrays.asList(
                new LabelingEnricher(),
                new VariablesEnricher(),
                new OptionalDependencyVariablesEnricher());


    }

    private static class LabelingEnricher extends EnricherExecutable {
        @Override
        protected void execute(EnricherExecutableParams params) {
            ComposeFileModel model = params.getModel();
            StackDescription stack = params.getStack();
            model.addLabelToServices("org.shathel.stack.gav", stack.getGav());
            model.addLabelToServices("org.shathel.stack.deployName", stack.getDeployName());
            model.addLabelToServices("org.shathel.stack.ga", stack.getGroup() + ":" + stack.getName());
            model.addLabelToServices("org.shathel.stack.marker", "true");
            model.addLabelToServices("org.shathel.deployer.version", versionInfo());
            List<StackDependency> dependencies = stack.getDependencies();
            int i = 0;
            for (StackDependency dependency : dependencies) {
                if (!dependency.isOptional()) {
                    model.addLabelToServices("org.shathel.stack.dependency." + i, dependency.getStackReference().getGav());
                }
                i++;
            }
            i = 0;
            for (StackDependency dependency : dependencies) {
                if (dependency.isOptional()) {
                    model.addLabelToServices("org.shathel.stack.dependency.optional." + i, dependency.getStackReference().getGav());
                }
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

    private static class VariablesEnricher extends EnricherExecutable {
        @Override
        protected void execute(EnricherExecutableParams params) {
            EnvironmentContext environmentContext = params.getEnvironmentContext();
            int size = environmentContext.getEnvironmentDescription().getNodesCount();
            int quorum = (int) Math.floor(size / 2) + 1;

            Map<String, String> environment = params.getEnvironment();
            environment.put("SHATHEL_ENV_SIZE", "" + size);
            environment.put("SHATHEL_ENV_QUORUM", "" + quorum);

            int msize = environmentContext.getEnvironmentDescription().getManagersCount();
            int mquorum = (int) Math.floor(msize / 2) + 1;

            environment.put("SHATHEL_ENV_MGM_SIZE", "" + msize);
            environment.put("SHATHEL_ENV_MGM_QUORUM", "" + mquorum);

            environment.put("SHATHEL_ENV_DOMAIN", environmentContext.getEnvironmentDescription().getParameter("domain-name").orElse("localhost"));
        }
    }


    private static class OptionalDependencyVariablesEnricher extends EnricherExecutable {
        @Override
        protected void execute(EnricherExecutableParams params) {
            StackDescription stack = params.getStack();
            Stack.StackContext stackContext = params.getStackContext();
            stack.getDependencies().stream()
                    //searching for dependencies that will be present in runtime
                    .filter(x -> params.isWithOptional() //if is with optional so everything will be
                            || !x.isOptional() //not optional deps for sure will be
                            || stackContext.getStackDescription(x.getStackReference()).isPresent() //at last existing in environment
                    ).forEach(x -> params.getEnvironment().putAll(x.getEnvs()));
        }


    }


}
