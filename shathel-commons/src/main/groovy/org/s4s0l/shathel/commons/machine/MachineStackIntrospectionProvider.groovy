package org.s4s0l.shathel.commons.machine

import org.s4s0l.shathel.commons.core.environment.StackIntrospection
import org.s4s0l.shathel.commons.core.environment.StackIntrospectionProvider
import org.s4s0l.shathel.commons.core.stack.StackReference

/**
 * @author Matcin Wielgus
 */
class MachineStackIntrospectionProvider implements StackIntrospectionProvider{
    @Override
    Optional<StackIntrospection> getIntrospection(StackReference reference) {
        return null
    }
}
