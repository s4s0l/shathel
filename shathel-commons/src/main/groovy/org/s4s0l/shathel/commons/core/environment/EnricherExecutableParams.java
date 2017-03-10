package org.s4s0l.shathel.commons.core.environment;

import org.s4s0l.shathel.commons.core.Stack;
import org.s4s0l.shathel.commons.core.model.ComposeFileModel;
import org.s4s0l.shathel.commons.core.stack.StackDescription;

import java.util.HashMap;
import java.util.Map;

public class EnricherExecutableParams {
    private final EnvironmentContext environmentContext;
    private final ExecutableApiFacade apiFacade;
    private final StackDescription stack;
    private final ComposeFileModel model;
    private final Map<String, String> environment;
    private final Stack.StackContext stackContext;
    private final boolean withOptional;

    public EnricherExecutableParams(Map<String, Object> map) {
        this(
                (EnvironmentContext) map.get("context"),
                (ExecutableApiFacade) map.get("api"),
                (StackDescription) map.get("stack"),
                (ComposeFileModel) map.get("compose"),
                (Map<String, String>) map.get("env"),
                (Stack.StackContext) map.get("stackContext"),
                (Boolean) map.get("withOptional")
        );
    }

    private EnricherExecutableParams(EnvironmentContext environmentContext,
                                     ExecutableApiFacade apiFacade,
                                     StackDescription stack,
                                     ComposeFileModel model,
                                     Map<String, String> environment,
                                     Stack.StackContext stackContext,
                                     boolean withOptional) {
        this.environmentContext = environmentContext;
        this.apiFacade = apiFacade;
        this.stack = stack;
        this.model = model;
        this.environment = environment;
        this.stackContext = stackContext;
        this.withOptional = withOptional;
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("context", environmentContext);
        map.put("env", environment);
        map.put("api", apiFacade);
        map.put("stack", stack);
        map.put("stackContext", stackContext);
        map.put("compose", model);
        map.put("withOptional", withOptional);
        return map;
    }

    public EnricherExecutableParams(StackDescription stack, ComposeFileModel model,
                                    Map<String, String> environment, Stack.StackContext stackContext,
                                    boolean withOptional) {
        this.environmentContext = stackContext.getEnvironment().getEnvironmentContext();
        this.apiFacade = stackContext.getEnvironment().getEnvironmentApiFacade();
        this.stack = stack;
        this.model = model;
        this.environment = environment;
        this.stackContext = stackContext;
        this.withOptional = withOptional;
    }

    public boolean isWithOptional() {
        return withOptional;
    }

    public EnvironmentContext getEnvironmentContext() {
        return environmentContext;
    }

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
}
