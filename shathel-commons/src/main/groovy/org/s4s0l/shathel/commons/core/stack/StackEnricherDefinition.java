package org.s4s0l.shathel.commons.core.stack;

/**
 * @author Matcin Wielgus
 */
public class StackEnricherDefinition extends ScriptDefinition {

    public boolean isApplicableTo(StackDescription forStack) {
        if (getOrigin().equals(forStack)) {
            return false;
        }
        if (target == Target.ALL) {
            return true;
        } else {
            return forStack.isDependantOn(getOrigin().getReference());
        }
    }

    public enum Target {
        ALL,
        DEPS
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
