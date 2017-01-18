package org.s4s0l.shathel.commons.core.environment;

/**
 * @author Matcin Wielgus
 */
public class EnvironmentDescription {
    private final String name;
    private final String type;

    public EnvironmentDescription(String name, String type) {
        this.name = name;
        this.type = type;
    }

    public String getType() {
        return type;
    }
}
