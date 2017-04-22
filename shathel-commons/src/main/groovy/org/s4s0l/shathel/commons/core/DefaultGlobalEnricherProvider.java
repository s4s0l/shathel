package org.s4s0l.shathel.commons.core;

import org.s4s0l.shathel.commons.core.environment.*;
import org.s4s0l.shathel.commons.core.model.ComposeFileModel;
import org.s4s0l.shathel.commons.core.stack.StackDependency;
import org.s4s0l.shathel.commons.core.stack.StackDescription;
import org.s4s0l.shathel.commons.scripts.NamedExecutable;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @author Marcin Wielgus
 */
public class DefaultGlobalEnricherProvider implements GlobalEnricherProvider {


    @Override
    public List<NamedExecutable> getGlobalEnrichers() {
        return Arrays.asList(
                new LabelingEnricher(),
                new VariablesEnricher(),
                new OptionalDependencyVariablesEnricher());


    }

    private static class LabelingEnricher extends EnricherExecutable {

        private void addLabels(ComposeFileModel model, String name, String value) {
            name = name.replace("$", "$$");
            value = value.replace("$", "$$");
            model.addLabelToServices(name, value);
            model.addLabelToNetworks(name, value);
            model.addLabelToVolumes(name, value);
        }

        @Override
        protected void execute(EnricherExecutableParams params) {
            ComposeFileModel model = params.getModel();
            StackDescription stack = params.getStack();
            addLabels(model, "org.shathel.stack.gav", stack.getGav());
            addLabels(model, "org.shathel.stack.deployName", stack.getDeployName());
            addLabels(model, "org.shathel.stack.ga", stack.getGroup() + ":" + stack.getName());
            addLabels(model, "org.shathel.stack.marker", "true");
            addLabels(model, "org.shathel.deployer.version", versionInfo());
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
            Map<String, String> environment = params.getEnvironment();
            environment.putAll(params.getStack().getEnvs());
            environment.putAll(params.getEnvironmentContext().getAsEnvironmentVariables());
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
