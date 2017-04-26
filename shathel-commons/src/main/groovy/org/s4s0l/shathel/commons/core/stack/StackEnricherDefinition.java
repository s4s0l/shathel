package org.s4s0l.shathel.commons.core.stack;

/**
 * @author Marcin Wielgus
 */
public class StackEnricherDefinition extends StackScriptDefinition {

    public boolean isApplicableTo(StackDescription forStack) {
        switch (target) {
            case ALL:
                return !getOrigin().equals(forStack);
            case DEPS:
                return !getOrigin().equals(forStack) && forStack.isDependantOn(getOrigin().getReference(), true);
            case ALLOTHERS:
                return !getOrigin().equals(forStack) && !getOrigin().isDependantOn(forStack.getReference(), true);
            case SELF:
                return getOrigin().equals(forStack);
            default:
                return false;
        }
    }

    public enum Target {
        ALL,
        DEPS,
        ALLOTHERS,
        SELF
    }


    private final Target target;

    public Target getTarget() {
        return target;
    }

    public StackEnricherDefinition(StackDescription origin, String name, String inline, String type, Target target) {
        super(origin, "enrichers", name, inline, type);
        this.target = target;
    }
}
