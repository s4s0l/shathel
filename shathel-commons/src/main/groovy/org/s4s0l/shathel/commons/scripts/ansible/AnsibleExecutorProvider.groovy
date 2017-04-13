package org.s4s0l.shathel.commons.scripts.ansible

import org.s4s0l.shathel.commons.bin.BinaryManager
import org.s4s0l.shathel.commons.scripts.NamedExecutable
import org.s4s0l.shathel.commons.scripts.ScriptExecutorProvider
import org.s4s0l.shathel.commons.scripts.TypedScript
import org.s4s0l.shathel.commons.scripts.groovy.GroovyExecutable
import org.s4s0l.shathel.commons.utils.ExtensionContext

/**
 * @author Marcin Wielgus
 */
class AnsibleExecutorProvider implements ScriptExecutorProvider {
    @Override
    Optional<NamedExecutable> findExecutable(ExtensionContext cntext, TypedScript typedScript) {
        if ("vagrant".equals(typedScript.getType())) {
            def one = cntext.lookupOne(BinaryManager)
            def locate = one.get().locate("vagrant-playbook")
            return Optional.<NamedExecutable> of(new AnsibleExecutable(typedScript, new AnsibleWrapper(locate)))
        } else {
            return Optional.empty()
        }
    }
}
