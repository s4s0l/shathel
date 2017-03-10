package org.s4s0l.shathel.commons.core;

import java.util.Optional;

/**
 * @author Marcin Wielgus
 */
public interface ParameterProvider {
    Optional<String> getParameter(String name);

    default Optional<Integer> getParameterAsInt(String name) {
        return getParameter(name)
                .map(Integer::parseInt);
    }

    default Optional<Boolean> getParameterAsBoolean(String name) {
        return getParameter(name)
                .map(Boolean::parseBoolean);
    }
}
