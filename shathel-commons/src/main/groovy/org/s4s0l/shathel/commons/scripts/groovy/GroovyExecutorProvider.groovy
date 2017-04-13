package org.s4s0l.shathel.commons.scripts.groovy

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import org.s4s0l.shathel.commons.scripts.NamedExecutable
import org.s4s0l.shathel.commons.scripts.ScriptExecutorProvider
import org.s4s0l.shathel.commons.scripts.TypedScript
import org.s4s0l.shathel.commons.utils.ExtensionContext

/**
 * @author Marcin Wielgus
 */
@TypeChecked
@CompileStatic
class GroovyExecutorProvider implements ScriptExecutorProvider {

    @Override
    Optional<NamedExecutable> findExecutable(ExtensionContext cntext, TypedScript typedScript) {
        if ("groovy".equals(typedScript.getType())) {
            return Optional.<NamedExecutable> of(new GroovyExecutable(typedScript))
        } else {
            return Optional.empty()
        }
    }
}
