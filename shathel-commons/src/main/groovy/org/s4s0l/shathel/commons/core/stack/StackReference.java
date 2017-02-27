package org.s4s0l.shathel.commons.core.stack;

import org.apache.commons.lang.StringUtils;
import org.s4s0l.shathel.commons.core.model.GavUtils;

/**
 * @author Matcin Wielgus
 */
public class StackReference {
    private final String group;
    private final String name;
    private final String version;

    public StackReference(String group, String name, String version) {
        this.name = name;
        this.group = group == null ? GavUtils.DEFAULT_GROUP : group;
        this.version = version;
    }

    public StackReference(String gav) {
        this(GavUtils.getGroup(gav), GavUtils.getName(gav), GavUtils.getVersion(gav));
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

    public String getSimpleName() {
        return new StringBuilder()
                .append(getName())
                .append("-")
                .append(getVersion())
                .toString();
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

    @Override
    public String toString() {
        return "StackReference{" +
                "group='" + group + '\'' +
                ", name='" + name + '\'' +
                ", version='" + version + '\'' +
                '}';
    }

    /**
     * Is same stack, comparest group and name only
     *
     * @param o other stack reference
     * @return true uf group and name matches
     */
    public boolean isSameStack(StackReference o) {
        return StringUtils.equals(getGroup(), o.getGroup()) &&
                StringUtils.equals(getName(), o.getName());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        StackReference that = (StackReference) o;

        if (group != null ? !group.equals(that.group) : that.group != null)
            return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        return version != null ? version.equals(that.version) : that.version == null;
    }

    @Override
    public int hashCode() {
        int result = group != null ? group.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (version != null ? version.hashCode() : 0);
        return result;
    }
}
