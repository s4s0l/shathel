package org.s4s0l.shathel.commons.scripts.ansible

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked

/**
 * @author Marcin Wielgus
 */
@TypeChecked
@CompileStatic
class AnsibleScriptContext {
    final String user
    final Optional<File> sshKey
    final Optional<String> sudoPasswordEnvName
    final File inventoryFile

    AnsibleScriptContext(String user, File sshKey, File inventoryFile) {
        this.user = user
        this.sshKey = Optional.of(sshKey)
        this.sudoPasswordEnvName = Optional.empty()
        this.inventoryFile = inventoryFile
    }

    AnsibleScriptContext(String user, String envPasswordName, File inventoryFile) {
        this.user = user
        this.sshKey = Optional.empty()
        this.sudoPasswordEnvName = Optional.of(envPasswordName)
        this.inventoryFile = inventoryFile
    }

}
