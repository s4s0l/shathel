package org.s4s0l.shathel.commons.core.stack;

/**
 * @author Marcin Wielgus
 */
public class StackEnricherDefinition extends StackScriptDefinition {

    public boolean isApplicableTo(StackDescription forStack) {
        if (getOrigin().equals(forStack)) {
            return false;
        }
        if (target == Target.ALL) {
            return true;
        } else if (target == Target.ALLOTHERS) {
            return !getOrigin().isDependantOn(forStack.getReference(), true);
        } else {
            return forStack.isDependantOn(getOrigin().getReference(), true);
        }
    }

    public enum Target {
        ALL,
        DEPS,
        ALLOTHERS
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
