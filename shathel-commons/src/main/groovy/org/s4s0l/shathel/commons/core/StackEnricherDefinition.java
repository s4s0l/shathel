package org.s4s0l.shathel.commons.core;

/**
 * @author Matcin Wielgus
 */
public class StackEnricherDefinition {


    public boolean isApplicableTo(StackDescription forStack) {
        return target == Target.ALL ? true : forStack.isDependantOn(origin.getReference());
    }

    public enum Target{
        ALL,
        DEPS
    }


    private final StackDescription origin;
    private final Target target;
    private final String name;
    private final String type;

    public Target getTarget() {
        return target;
    }

    public StackEnricherDefinition(StackDescription origin, Target target, String name, String type) {
        this.origin = origin;
        this.target = target;
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
