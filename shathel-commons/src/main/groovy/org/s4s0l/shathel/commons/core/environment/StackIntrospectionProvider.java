package org.s4s0l.shathel.commons.core.environment;

import org.s4s0l.shathel.commons.core.stack.StackReference;

import java.util.Optional;

/**
 * @author Matcin Wielgus
 */
public interface StackIntrospectionProvider {

    Optional<StackIntrospection> getIntrospection(StackReference reference);

}
