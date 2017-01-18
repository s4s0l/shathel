package org.s4s0l.shathel.commons.core.stack;

/**
 * @author Matcin Wielgus
 */
public class StackReference {
    private final String group;
    private final String name;
    private final String version;

    public StackReference(String group, String name, String version) {
        this.name = name;
        this.group = group;
        this.version = version;
    }
    public StackReference(String gav) {
        this(gav.split(":")[0], gav.split(":")[1], gav.split(":")[2] );
    }
    public String getName() {
        return name;
    }

    public String getVersion() {
        return version;
    }

    public String getGroup() {
        return group;
    }


    public String getStackDirecctoryName() {
        return new StringBuilder()
                .append(getName())
                .append("-")
                .append(getVersion())
                .append("-shathel").toString();
    }

    public String getStackFileName() {
        return new StringBuilder()
                .append(getName())
                .append("-")
                .append(getVersion())
                .append("-shathel.zip").toString();
    }

    public String getGav() {
        return getGroup() + ":" + getName() + ":" + getVersion();
    }
}
