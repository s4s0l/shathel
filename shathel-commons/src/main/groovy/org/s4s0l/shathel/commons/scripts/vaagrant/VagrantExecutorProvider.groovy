package org.s4s0l.shathel.commons.scripts.vaagrant

import org.s4s0l.shathel.commons.bin.BinaryManager
import org.s4s0l.shathel.commons.bin.BinaryManagerExtensionManager
import org.s4s0l.shathel.commons.scripts.NamedExecutable
import org.s4s0l.shathel.commons.scripts.ScriptExecutorProvider
import org.s4s0l.shathel.commons.scripts.TypedScript
import org.s4s0l.shathel.commons.scripts.ansible.AnsibleExecutable
import org.s4s0l.shathel.commons.scripts.ansible.AnsibleWrapper
import org.s4s0l.shathel.commons.utils.ExtensionContext

/**
 * @author Marcin Wielgus
 */
class VagrantExecutorProvider implements ScriptExecutorProvider {
    @Override
    Optional<NamedExecutable> findExecutable(ExtensionContext cntext, TypedScript typedScript) {
        if ("vagrant".equals(typedScript.getType())) {
            def one = cntext.lookupOne(BinaryManagerExtensionManager)
            def locate = one.orElseThrow {
                new RuntimeException("Binaries manager missing")
            }.getManager(cntext).locate("vagrant")
            return Optional.<NamedExecutable> of(new VagrantExecutable(typedScript, new VagrantWrapper(locate)))
        } else {
            return Optional.empty()
        }
    }
}