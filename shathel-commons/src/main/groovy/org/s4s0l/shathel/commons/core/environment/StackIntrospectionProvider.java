package org.s4s0l.shathel.commons.core.environment;

import org.s4s0l.shathel.commons.core.stack.StackReference;

import java.util.List;
import java.util.Optional;

/**
 * @author Matcin Wielgus
 */
public interface StackIntrospectionProvider {

    Optional<StackIntrospection> getStackIntrospection(StackReference reference);

    StackIntrospections getAllStacks();


    class StackIntrospections {
        private final List<StackIntrospection> stacks;

        public StackIntrospections(List<StackIntrospection> stacks) {
            this.stacks = stacks;
        }

        public List<StackIntrospection> getStacks() {
            return stacks;
        }

        public Optional<StackIntrospection> getIntrospection(StackReference reference) {
            return stacks.stream().filter(x -> x.getReference().isSameStack(reference)).findFirst();
        }

        public int size() {
            return stacks.size();
        }
    }
}
