package org.s4s0l.shathel.commons.core;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Marcin Wielgus
 */
public interface Parameters extends ParameterProvider {

    default Parameters hiddenBy(Parameters x) {
        return new MultiSourceParameters(Arrays.asList(x, this));
    }

    default Parameters hiddenBySystemProperties() {
        return hiddenBy(new SystemParameters());
    }

    static String parameterNameToEnvName(String parameterName) {
        return parameterName.toUpperCase().replaceAll("[^A-Z0-9\\-]", "_");
    }

    static String getNormalizedParameterName(String key) {
        return key.toLowerCase().replaceAll("[^a-z0-9\\-]", ".");
    }

    static Map<String, String> getNormalizedParameterNames(Map<String, String> parameters) {
        Map<String, String> ret = new HashMap<>();
        for (Map.Entry<String, String> e : parameters.entrySet()) {
            ret.put(Parameters.getNormalizedParameterName(e.getKey().toString()), e.getValue().toString());
        }
        return ret;
    }

    default Parameters hiddenByVariables() {
        return hiddenBy(new EnvParameters());
    }

    Set<String> getAllParameters();

    static Parameters fromMapWithSysPropAndEnv(Map<String, String> map) {
        return MapParameters.builder().parameters(map).build().hiddenBySystemProperties().hiddenByVariables();
    }

    class EnvParameters implements Parameters {
        @Override
        public Optional<String> getParameter(String name) {
            String normalized = getNormalizedParameterName(name);
            return System.getenv().entrySet()
                    .stream()
                    .filter(it -> getNormalizedParameterName(it.getKey()).startsWith("shathel"))
                    .filter(it -> getNormalizedParameterName(it.getKey()).equals(normalized))
                    .findFirst()
                    .map(it -> it.getValue());
        }

        @Override
        public Set<String> getAllParameters() {
            return System.getenv().keySet()
                    .stream()
                    .filter(it -> getNormalizedParameterName(it).startsWith("shathel"))
                    .map(x -> getNormalizedParameterName(x))
                    .collect(Collectors.toSet());
        }
    }

    class SystemParameters implements Parameters {
        @Override
        public Optional<String> getParameter(String name) {
            String normalized = getNormalizedParameterName(name);
            return System.getProperties().entrySet()
                    .stream()
                    .filter(x -> x.getValue() instanceof String && x.getKey() instanceof String)
                    .map(x -> new AbstractMap.SimpleEntry<>((String) x.getKey(), (String) x.getValue()))
                    .filter(it -> getNormalizedParameterName(it.getKey()).startsWith("shathel"))
                    .filter(it -> getNormalizedParameterName(it.getKey()).equals(normalized))
                    .findFirst()
                    .map(it -> it.getValue());
        }

        @Override
        public Set<String> getAllParameters() {
            return System.getProperties().keySet()
                    .stream()
                    .filter(x -> x instanceof String)
                    .map(x -> (String) x)
                    .map(it -> getNormalizedParameterName(it))
                    .filter(x -> x.startsWith("shathel"))
                    .collect(Collectors.toSet());
        }
    }
}
