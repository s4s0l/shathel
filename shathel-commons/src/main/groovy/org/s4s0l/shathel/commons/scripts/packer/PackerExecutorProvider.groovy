package org.s4s0l.shathel.commons.scripts.packer

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import org.s4s0l.shathel.commons.bin.BinaryManagerExtensionManager
import org.s4s0l.shathel.commons.scripts.NamedExecutable
import org.s4s0l.shathel.commons.scripts.ScriptExecutorProvider
import org.s4s0l.shathel.commons.scripts.TypedScript
import org.s4s0l.shathel.commons.scripts.terraform.TerraformExecutable
import org.s4s0l.shathel.commons.scripts.terraform.TerraformWrapper
import org.s4s0l.shathel.commons.utils.ExtensionContext

/**
 * @author Marcin Wielgus
 */
@TypeChecked
@CompileStatic
class PackerExecutorProvider implements ScriptExecutorProvider {
    @Override
    Optional<NamedExecutable> findExecutable(ExtensionContext cntext, TypedScript typedScript) {
        if ("packer".equals(typedScript.getType())) {
            def one = cntext.lookupOne(BinaryManagerExtensionManager)
            def locate = one.orElseThrow {
                new RuntimeException("Binaries manager missing")
            }.getManager(cntext).locate("packer")
            return Optional.<NamedExecutable> of(new PackerExecutable(typedScript, new PackerWrapper(locate)))
        } else {
            return Optional.empty()
        }
    }
}