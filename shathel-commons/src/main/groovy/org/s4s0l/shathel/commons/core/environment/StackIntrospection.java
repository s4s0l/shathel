package org.s4s0l.shathel.commons.core.environment;

import org.s4s0l.shathel.commons.core.stack.StackReference;

import java.util.Collections;
import java.util.Map;

/**
 * @author Matcin Wielgus
 */
public class StackIntrospection {
    private final StackReference reference;

    private final Map<String, String> labels;

    public StackIntrospection(StackReference reference, Map<String, String> labels) {
        this.reference = reference;
        this.labels = labels;
    }

    public StackReference getReference() {
        return reference;
    }

    public Map<String, String> getLabels() {
        return Collections.unmodifiableMap(labels);
    }
}
