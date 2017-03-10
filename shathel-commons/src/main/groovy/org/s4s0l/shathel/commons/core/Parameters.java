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

    default Parameters over(Parameters x) {
        return new MultiSourceParameters(Arrays.asList(this, x));
    }

    default Parameters overSystemProperties() {
        return over(new Parameters() {
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
        });
    }

    default Parameters overEnvVariables() {
        return over(new Parameters() {
            @Override
            public Optional<String> getParameter(String name) {
                return Optional.ofNullable(System.getenv(name.toUpperCase().replaceAll("\\.", "_")));
            }

            @Override
            public Set<String> getAllParameters() {
                return System.getenv().keySet()
                        .stream()
                        .filter(x -> x.startsWith("SHATHEL_"))
                        .map(x->x.toLowerCase().replace("_", "."))
                        .collect(Collectors.toSet());
            }
        });
    }

    Set<String> getAllParameters();

    static Parameters fromMapWithSysPropAndEnv(Map<String, String> map) {
        return MapParameters.builder().parameters(map).build().overSystemProperties().overEnvVariables();
    }

}
