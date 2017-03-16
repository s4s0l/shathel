package org.s4s0l.shathel.commons.core.dependencies;

import org.s4s0l.shathel.commons.core.stack.StackReference;

import java.util.Optional;

/**
 * @author Marcin Wielgus
 */
public class StackLocator {
    private final String location;
    private final Optional<StackReference> reference;

    public StackLocator(String location) {
        this.location = location;
        this.reference = Optional.empty();
    }

    public StackLocator(StackReference reference) {
        this.reference = Optional.of(reference);
        location = reference.getGav();
    }

    public Optional<StackReference> getReference() {
        return reference;
    }

    public String getLocation() {
        return location;
    }
}
