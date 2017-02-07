package org.s4s0l.shathel.commons.core.environment;

import org.s4s0l.shathel.commons.core.Parameters;

import java.util.Map;
import java.util.Optional;

/**
 * @author Matcin Wielgus
 */
public class EnvironmentDescription {
    private final Parameters overrides;
    private final String name;
    private final String type;
    private final Map<String, String> parameters;

    public EnvironmentDescription(Parameters overrides, String name, String type, Map<String, String> parameters) {
        this.overrides = overrides;
        this.name = name;
        this.type = type;
        this.parameters = parameters;
    }

    public String getName() {
        return name;
    }

    public Optional<String> getParameter(String name) {
        String s = overrides.getParameter("shathel.env." + getName() + "." + name).orElseGet(() -> parameters.get(name));
        return Optional.ofNullable(s);
    }

    public String getType() {
        return type;
    }

    public Optional<Integer> getParameterAsInt(String name) {
        return getParameter(name)
                .map(Integer::parseInt);
    }
}
