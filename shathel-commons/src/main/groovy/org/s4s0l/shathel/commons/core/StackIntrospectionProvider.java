package org.s4s0l.shathel.commons.core;

import java.util.Optional;

/**
 * @author Matcin Wielgus
 */
public interface StackIntrospectionProvider {

    Optional<StackIntrospection> getIntrospection(StackReference reference);

}
