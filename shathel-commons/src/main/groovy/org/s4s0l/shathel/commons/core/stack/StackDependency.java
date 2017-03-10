package org.s4s0l.shathel.commons.core.stack;

import java.util.Map;

/**
 * @author Marcin Wielgus
 */
public class StackDependency {
    private final StackReference stackReference;
    private final boolean optional;
    private final Map<String,String> envs;

    public StackDependency(StackReference stackReference, boolean optional, Map<String, String> envs) {
        this.stackReference = stackReference;
        this.optional = optional;
        this.envs = envs;
    }

    public StackReference getStackReference() {
        return stackReference;
    }

    public boolean isOptional() {
        return optional;
    }

    public Map<String, String> getEnvs() {
        return envs;
    }
}
