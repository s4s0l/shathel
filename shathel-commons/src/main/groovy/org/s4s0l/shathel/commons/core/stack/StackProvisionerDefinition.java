package org.s4s0l.shathel.commons.core.stack;

/**
 * @author Matcin Wielgus
 */
public class StackProvisionerDefinition {
    public final String name;
    public final String type;

    public StackProvisionerDefinition(String name, String type) {
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
