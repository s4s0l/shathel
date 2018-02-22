package org.s4s0l.shathel.commons.core.environment;

import org.s4s0l.shathel.commons.core.Stack;
import org.s4s0l.shathel.commons.core.model.ComposeFileModel;
import org.s4s0l.shathel.commons.core.stack.StackDescription;
import org.s4s0l.shathel.commons.scripts.NamedExecutable;
import org.s4s0l.shathel.commons.scripts.ScriptExecutorProvider;
import org.s4s0l.shathel.commons.scripts.TypedScript;
import org.s4s0l.shathel.commons.utils.ExtensionContext;
import org.slf4j.Logger;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class EnricherExecutableParams {
    private final Logger logger;
    private final EnvironmentContext environmentContext;
    private final ExecutableApiFacade apiFacade;
    private final StackDescription stack;
    private final ComposeFileModel model;
    private final Map<String, String> environment;
    private final Stack.StackContext stackContext;
    private final Provisioners provisioners;
    private final boolean withOptional;

    public EnricherExecutableParams(Map<String, Object> map) {
        this(
                (Logger) map.get("log"),
                (EnvironmentContext) map.get("context"),
                (ExecutableApiFacade) map.get("api"),
                (StackDescription) map.get("stack"),
                (ComposeFileModel) map.get("compose"),
                (Map<String, String>) map.get("env"),
                (Stack.StackContext) map.get("stackContext"),
                (Boolean) map.get("withOptional"),
                (Provisioners) map.get("provisioners")
        );
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("log", logger);
        map.put("context", environmentContext);
        map.put("api", apiFacade);
        map.put("env", environment);
        map.put("stack", stack);
        map.put("stackContext", stackContext);
        map.put("compose", model);
        map.put("withOptional", withOptional);
        map.put("provisioners", provisioners);
        return map;
    }


    private EnricherExecutableParams(Logger logger, EnvironmentContext environmentContext,
                                     ExecutableApiFacade apiFacade,
                                     StackDescription stack,
                                     ComposeFileModel model,
                                     Map<String, String> environment,
                                     Stack.StackContext stackContext,
                                     boolean withOptional,
                                     Provisioners provisioners) {
        this.logger = logger;
        this.environmentContext = environmentContext;
        this.apiFacade = apiFacade;
        this.stack = stack;
        this.model = model;
        this.environment = environment;
        this.stackContext = stackContext;
        this.withOptional = withOptional;
        this.provisioners = provisioners;
    }


    public EnricherExecutableParams(Logger logger, StackDescription stack, ComposeFileModel model,
                                    Map<String, String> environment, Stack.StackContext stackContext,
                                    boolean withOptional, Provisioners provisioners) {
        this.logger = logger;
        this.environmentContext = stackContext.getEnvironment().getEnvironmentContext();
        this.apiFacade = stackContext.getEnvironment().getEnvironmentApiFacade();
        this.stack = stack;
        this.model = model;
        this.environment = environment;
        this.stackContext = stackContext;
        this.withOptional = withOptional;
        this.provisioners = provisioners;
    }

    public boolean isWithOptional() {
        return withOptional;
    }

    public EnvironmentContext getEnvironmentContext() {
        return environmentContext;
    }


    //todo: change to getApi to be consistent with map key names
    public ExecutableApiFacade getApiFacade() {
        return apiFacade;
    }

    public StackDescription getStack() {
        return stack;
    }

    public ComposeFileModel getModel() {
        return model;
    }

    public Map<String, String> getEnvironment() {
        return environment;
    }

    public Stack.StackContext getStackContext() {
        return stackContext;
    }

    public Provisioners getProvisioners() {
        return provisioners;
    }


    public static class Provisioners extends ArrayList<NamedExecutable> {
        private final ExtensionContext extensionContext;
        private final File parentScriptBaseLocation;
        private final String parentScriptName;


        public Provisioners(ExtensionContext scriptProvider, File parentScriptBaseLocation, String parentScriptName) {
            this.extensionContext = scriptProvider;
            this.parentScriptBaseLocation = parentScriptBaseLocation;
            this.parentScriptName = parentScriptName;
        }

        public void addAnsible(String file) {
            addAnsible(file, new HashMap<>());
        }

        public void addAnsible(String file, Map<String, String> additionalEnvironments) {

            Optional<NamedExecutable> ansibleExecutable = ScriptExecutorProvider.findExecutor(extensionContext, new TypedScript() {
                @Override
                public String getType() {
                    return "ansible";
                }

                @Override
                public String getScriptContents() {
                    throw new RuntimeException("no ansible inlining here");
                }

                @Override
                public String getScriptName() {
                    return parentScriptName + ":ansible:" + file;
                }

                @Override
                public Optional<File> getScriptFileLocation() {
                    return Optional.of(new File(parentScriptBaseLocation, file));
                }
            });
            NamedExecutable mapped = ansibleExecutable
                    .map(x -> new NamedExecutable() {
                        @Override
                        public TypedScript getScript() {
                            return x.getScript();
                        }

                        @Override
                        public void execute(Map<String, Object> context) {
                            new ProvisionerExecutableParams(context).getEnv().putAll(additionalEnvironments);
                            x.execute(context);
                        }

                        @Override
                        public String getName() {
                            return x.getName();
                        }
                    })
                    .orElseThrow(() -> new RuntimeException("ansible unsupported here?"));

            add(mapped);
        }

        public void add(String provisionerName, ProvisionerExecutable executable) {
            add(new NamedExecutable() {
                @Override
                public void execute(Map<String, Object> context) {
                    executable.execute(new ProvisionerExecutableParams(context));
                }

                @Override
                public String getName() {
                    return provisionerName;
                }

                @Override
                public TypedScript getScript() {
                    return new TypedScript() {
                        @Override
                        public String getType() {
                            return "groovy-inlined";
                        }

                        @Override
                        public String getScriptContents() {
                            throw new RuntimeException("no script contents for groovy-inlined script");
                        }

                        @Override
                        public String getScriptName() {
                            return getName();
                        }

                        @Override
                        public Optional<File> getScriptFileLocation() {
                            return Optional.empty();
                        }
                    };
                }
            });
        }

    }
}
