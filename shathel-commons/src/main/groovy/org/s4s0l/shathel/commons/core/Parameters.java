package org.s4s0l.shathel.commons.core;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;

/**
 * @author Matcin Wielgus
 */
public interface Parameters {
    Optional<String> getParameter(String name);

    default Parameters over(Parameters x) {
        return new MultiSourceParameters(Arrays.asList(this, x));
    }

    default Parameters overSystemProperties() {
        return over(name -> Optional.ofNullable(System.getProperty(name)));
    }

    default Parameters overEnvVariables() {
        return over(name -> Optional.ofNullable(System.getenv(name.toUpperCase().replaceAll("\\.", "_"))));
    }

    static MapParameters.MapParametersBuilder builder() {
        return MapParameters.builder();
    }

    static Parameters fromMapWithSysPropAndEnv(Map<String, String> map) {
        return builder().parameters(map).build().overSystemProperties().overEnvVariables();
    }

}
