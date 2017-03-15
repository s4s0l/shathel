package org.s4s0l.shathel.commons.core;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
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
        return parameterName.toUpperCase().replaceAll("\\.", "_");
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
            return Optional.ofNullable(System.getenv(parameterNameToEnvName(name)));
        }

        @Override
        public Set<String> getAllParameters() {
            return System.getenv().keySet()
                    .stream()
                    .filter(x -> x.startsWith("SHATHEL_"))
                    .map(x -> x.toLowerCase().replace("_", "."))
                    .collect(Collectors.toSet());
        }
    }

    class SystemParameters implements Parameters {
        @Override
        public Optional<String> getParameter(String name) {
            return Optional.ofNullable(System.getProperty(name));
        }

        @Override
        public Set<String> getAllParameters() {
            return System.getProperties().keySet()
                    .stream()
                    .filter(x -> x instanceof String)
                    .map(x -> (String) x)
                    .filter(x -> x.startsWith("shathel"))
                    .collect(Collectors.toSet());
        }
    }
}
