package org.s4s0l.shathel.commons.core.environment;

import org.s4s0l.shathel.commons.core.stack.StackReference;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author Marcin Wielgus
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


    public List<Service> getServices() {
        return services;
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
        final Map<Integer, Integer> portMapping;
        private final String stackDeployName;

        public Service(String stackDeployName, String serviceName, int currentInstances, int requiredInstances, Map<Integer, Integer> portMapping) {
            this.serviceName = serviceName;
            this.stackDeployName = stackDeployName;
            this.currentInstances = currentInstances;
            this.requiredInstances = requiredInstances;
            this.portMapping = Collections.unmodifiableMap(portMapping);
        }

        public int getPort(int targetPort) {
            return Optional.ofNullable(portMapping.get(targetPort))
                    .orElse(targetPort);
        }

        public String getFullServiceName() {
            return serviceName;
        }

        public String getServiceName() {
            if (serviceName.startsWith(stackDeployName + "_")) {
                return serviceName.substring((stackDeployName + "_").length());
            } else {
                return serviceName;
            }
        }

        public Map<Integer, Integer> getPortMapping() {
            return portMapping;
        }

        public int getCurrentInstances() {
            return currentInstances;
        }

        public int getRequiredInstances() {
            return requiredInstances;
        }
    }
}
