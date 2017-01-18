package org.s4s0l.shathel.commons.core.stack;

/**
 * @author Matcin Wielgus
 */
public class StackEnricherDefinition {


    public boolean isApplicableTo(StackDescription forStack) {
        if (origin.equals(forStack)) {
            return false;
        }
        if (target == Target.ALL) {
            return true;
        } else {
            return forStack.isDependantOn(origin.getReference());
        }
    }

    public enum Target {
        ALL,
        DEPS
    }


    private final StackDescription origin;
    private final Target target;
    private final String name;
    private final String inline;
    private final String type;

    public Target getTarget() {
        return target;
    }

    public StackEnricherDefinition(StackDescription origin, Target target, String name, String inline, String type) {
        this.origin = origin;
        this.target = target;
        this.name = name;
        this.inline = inline;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public String getInline() {
        return inline;
    }
}
