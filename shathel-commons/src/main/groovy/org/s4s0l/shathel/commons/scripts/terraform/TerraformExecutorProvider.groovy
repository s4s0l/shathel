package org.s4s0l.shathel.commons.scripts.terraform

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import org.s4s0l.shathel.commons.bin.BinaryManagerExtensionManager
import org.s4s0l.shathel.commons.scripts.NamedExecutable
import org.s4s0l.shathel.commons.scripts.ScriptExecutorProvider
import org.s4s0l.shathel.commons.scripts.TypedScript
import org.s4s0l.shathel.commons.scripts.vaagrant.VagrantExecutable
import org.s4s0l.shathel.commons.scripts.vaagrant.VagrantWrapper
import org.s4s0l.shathel.commons.utils.ExtensionContext

/**
 * @author Marcin Wielgus
 */
@TypeChecked
@CompileStatic
class TerraformExecutorProvider implements ScriptExecutorProvider {
    @Override
    Optional<NamedExecutable> findExecutable(ExtensionContext cntext, TypedScript typedScript) {
        if ("terraform".equals(typedScript.getType())) {
            def one = cntext.lookupOne(BinaryManagerExtensionManager)
            def locate = one.orElseThrow {
                new RuntimeException("Binaries manager missing")
            }.getManager(cntext).locate("terraform")
            return Optional.<NamedExecutable> of(new TerraformExecutable(typedScript, new TerraformWrapper(locate)))
        } else {
            return Optional.empty()
        }
    }
}