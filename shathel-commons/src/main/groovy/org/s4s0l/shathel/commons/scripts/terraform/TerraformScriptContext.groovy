package org.s4s0l.shathel.commons.scripts.terraform

/**
 * @author Marcin Wielgus
 */
class TerraformScriptContext {
    final File stateFile

    TerraformScriptContext(File stateFile) {
        this.stateFile = stateFile
    }
}
