package org.s4s0l.shathel.commons.core.environment;

import java.util.Map;
import java.util.Optional;

/**
 * @author Matcin Wielgus
 */
public class EnvironmentDescription {
    private final String name;
    private final String type;
    private final Map<String, String> parameters;

    public EnvironmentDescription(String name, String type, Map<String, String> parameters) {
        this.name = name;
        this.type = type;
        this.parameters = parameters;
    }

    public String getName() {
        return name;
    }

    public Optional<String> getParameter(String name) {
        return Optional.ofNullable(parameters.get(name));
    }

    public String getType() {
        return type;
    }

    public Optional<Integer> getParameterAsInt(String name) {
        return getParameter(name).map(Integer::parseInt);
    }
}
