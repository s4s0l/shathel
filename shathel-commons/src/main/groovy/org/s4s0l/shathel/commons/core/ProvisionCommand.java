package org.s4s0l.shathel.commons.core;

/**
 * @author Matcin Wielgus
 */
public class ProvisionCommand {
    private final String name;
    private final String type;


    public ProvisionCommand(String name, String type) {
        this.name = name;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }
}
