package org.s4s0l.shathel.commons.core.environment;

import org.s4s0l.shathel.commons.core.stack.StackReference;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author Marcin Wielgus
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

        /**
         * returns all stacks no one depends on
         *
         * @return only stacks which none depends on
         */
        public List<StackIntrospection> getRootStacks() {
            return stacks.stream()
                    .filter(it -> !stacks.stream().filter(itt -> itt.isDependantOn(it.getReference())).findAny().isPresent())
                    .collect(Collectors.toList());
        }

        public int size() {
            return stacks.size();
        }
    }
}
