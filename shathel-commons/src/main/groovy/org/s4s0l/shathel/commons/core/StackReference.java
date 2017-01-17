package org.s4s0l.shathel.commons.core;

/**
 * @author Matcin Wielgus
 */
public class StackReference {
    private final String name;
    private final String group;
    private final String version;

    public StackReference(String name, String group, String version) {
        this.name = name;
        this.group = group;
        this.version = version;
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


    public String getStackDirecctoryName(){
        return new StringBuilder()
                .append(getName())
                .append("-")
                .append(getVersion())
                .append("-shathel").toString();
    }

    public String getStackFileName(){
        return new StringBuilder()
                .append(getName())
                .append("-")
                .append(getVersion())
                .append("-shathel.zip").toString();
    }
}
