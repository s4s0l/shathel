package org.s4s0l.shathel.commons.scripts.ansible

/**
 * @author Marcin Wielgus
 */
class AnsibleScriptContext {
    final String user
    final File sshKey
    final File inventoryFile

    AnsibleScriptContext(String user, File sshKey, File inventoryFile) {
        this.user = user
        this.sshKey = sshKey
        this.inventoryFile = inventoryFile
    }

}
