package org.s4s0l.shathel.commons.core.environment;

import java.util.Optional;

/**
 * @author Marcin Wielgus
 */
public interface EnvironmentContextInternal {
    Optional<String> getEnvironmentParameter(String name);

    Optional<Integer> getEnvironmentParameterAsInt(String name);

    Optional<Boolean> getEnvironmentParameterAsBoolean(String name);
}
