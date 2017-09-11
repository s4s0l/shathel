package org.s4s0l.shathel.commons.core;

import org.s4s0l.shathel.commons.core.environment.*;
import org.s4s0l.shathel.commons.core.model.ComposeFileModel;
import org.s4s0l.shathel.commons.core.security.SafeStorage;
import org.s4s0l.shathel.commons.core.security.SimpleEncryptor;
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


    public static final String LABEL_PREFIX_SHATHEL = "org.shathel.stack";
    public static final String LABEL_SHATHEL_STACK_GAV = LABEL_PREFIX_SHATHEL + ".gav";
    public static final String LABEL_SHATHEL_STACK_DEPLOY_NAME = LABEL_PREFIX_SHATHEL + ".deployName";
    public static final String LABEL_SHATHEL_STACK_GA = LABEL_PREFIX_SHATHEL + ".ga";
    public static final String LABEL_SHATHEL_STACK_MARKER = LABEL_PREFIX_SHATHEL + ".marker";
    public static final String LABEL_SHATHEL_DEPLOYER_VERSION = "org.shathel.deployer.version";
    public static final String LABEL_PREFIX_SHATHEL_STACK_DEPENDENCY = LABEL_PREFIX_SHATHEL + ".dependency";
    public static final String LABEL_PREFIX_SHATHEL_STACK_DEPENDENCY_OPTIONAL = LABEL_PREFIX_SHATHEL_STACK_DEPENDENCY + ".optional";

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
            addLabels(model, LABEL_SHATHEL_STACK_GAV, stack.getGav());
            addLabels(model, LABEL_SHATHEL_STACK_DEPLOY_NAME, stack.getDeployName());
            addLabels(model, LABEL_SHATHEL_STACK_GA, stack.getReference().getGa());
            addLabels(model, LABEL_SHATHEL_STACK_MARKER, "true");
            addLabels(model, LABEL_SHATHEL_DEPLOYER_VERSION, versionInfo());
            List<StackDependency> dependencies = stack.getDependencies();
            int i = 0;
            for (StackDependency dependency : dependencies) {
                if (!dependency.isOptional()) {
                    model.addLabelToServices(LABEL_PREFIX_SHATHEL_STACK_DEPENDENCY + "." + i, dependency.getStackReference().getGa());
                }
                i++;
            }
            i = 0;
            for (StackDependency dependency : dependencies) {
                if (dependency.isOptional()) {
                    model.addLabelToServices(LABEL_PREFIX_SHATHEL_STACK_DEPENDENCY_OPTIONAL + "." + i, dependency.getStackReference().getGa());
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
            SimpleEncryptor safeStorage = params.getEnvironmentContext().getSafeStorage();
            environment.putAll(safeStorage.fixValues(params.getStack().getEnvs()));
            environment.putAll(safeStorage.fixValues(params.getEnvironmentContext().getAsEnvironmentVariables()));
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
