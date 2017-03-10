package org.s4s0l.shathel.commons.core.environment;

import org.s4s0l.shathel.commons.core.stack.StackReference;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author Matcin Wielgus
 */
public class StackIntrospection {
    private final StackReference reference;
    private final List<Service> services;
    private final Map<String, String> labels;

    public StackIntrospection(StackReference reference, List<Service> services, Map<String, String> labels) {
        this.reference = reference;
        this.services = services;
        this.labels = labels;
    }

    public StackReference getReference() {
        return reference;
    }

    public Map<String, String> getLabels() {
        return Collections.unmodifiableMap(labels);
    }

    public static class Service {
        final String serviceName;
        final int currentInstances;
        final int requiredInstances;

        public Service(String serviceName, int currentInstances, int requiredInstances) {
            this.serviceName = serviceName;
            this.currentInstances = currentInstances;
            this.requiredInstances = requiredInstances;
        }

        public String getServiceName() {
            return serviceName;
        }

        public int getCurrentInstances() {
            return currentInstances;
        }

        public int getRequiredInstances() {
            return requiredInstances;
        }
    }
}
