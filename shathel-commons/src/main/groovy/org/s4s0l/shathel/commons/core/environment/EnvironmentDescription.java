package org.s4s0l.shathel.commons.core.environment;

import org.s4s0l.shathel.commons.core.ParameterProvider;

import java.util.Map;
import java.util.Optional;

/**
 * @author Matcin Wielgus
 */
public class EnvironmentDescription implements ParameterProvider {
    private final ParameterProvider overrides;
    private final String name;
    private final String type;
    private final Map<String, String> parameters;

    public EnvironmentDescription(ParameterProvider overrides, String name, String type, Map<String, String> parameters) {
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

    public int getNodesCount() {
        return getManagersCount() + getWorkersCount();
    }

    public int getManagersCount() {
        return getParameterAsInt("managers")
                .orElse(1);
    }

    public int getWorkersCount() {
        return getParameterAsInt("workers")
                .orElse(0);
    }

    public String getType() {
        return type;
    }


}
