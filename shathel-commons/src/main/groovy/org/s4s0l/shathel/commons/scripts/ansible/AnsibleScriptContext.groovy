package org.s4s0l.shathel.commons.scripts.ansible

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked

/**
 * @author Marcin Wielgus
 */
@TypeChecked
@CompileStatic
class AnsibleScriptContext {
    final Optional<String> disabledMessage
    final Optional<String> user
    final Optional<File> sshKey
    final Optional<String> sudoPasswordEnvName
    final Optional<File> inventoryFile

    AnsibleScriptContext(String user, File sshKey, File inventoryFile) {
        this.user = Optional.of(user)
        this.sshKey = Optional.of(sshKey)
        this.sudoPasswordEnvName = Optional.empty()
        this.inventoryFile = Optional.of(inventoryFile)
        this.disabledMessage = Optional.empty()
    }

    AnsibleScriptContext(String user, String envPasswordName, File inventoryFile) {
        this.user = Optional.of(user)
        this.sshKey = Optional.empty()
        this.sudoPasswordEnvName = Optional.of(envPasswordName)
        this.inventoryFile = Optional.of(inventoryFile)
        this.disabledMessage = Optional.empty()
    }

    AnsibleScriptContext(String disabledMessage) {
        this.user = Optional.empty()
        this.sshKey = Optional.empty()
        this.sudoPasswordEnvName = Optional.empty()
        this.inventoryFile = Optional.empty()
        this.disabledMessage = Optional.of(disabledMessage)
    }

    boolean isDisabled() {
        return disabledMessage.isPresent()
    }

    void customize(Map<String, String> env) {
        sudoPasswordEnvName.ifPresent {
            if (env.containsKey(it))
                env['ANSIBLE_BECOME_PASS'] = env[it]
        }
    }

    Map<String, String> getArguments() {
        Map<String, String> ret = [:]
        user.ifPresent {String it -> ret.put("user", it) }
        ret.put("timeout", "180")
        sshKey.ifPresent { File it -> ret.put("private-key", it.absolutePath) }
        inventoryFile.ifPresent { File it -> ret.put("inventory-file", it.absolutePath) }
        return ret
    }


}
