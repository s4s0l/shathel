package org.s4s0l.shathel.commons.scripts.ansible

import org.s4s0l.shathel.commons.core.environment.EnvironmentContext
import org.s4s0l.shathel.commons.core.environment.ShathelNode
import org.s4s0l.shathel.commons.scripts.NamedExecutable
import org.s4s0l.shathel.commons.scripts.TypedScript

/**
 * @author Marcin Wielgus
 */
class AnsibleExecutable implements NamedExecutable {
    private final TypedScript script
    private final AnsibleWrapper ansible

    AnsibleExecutable(TypedScript script, AnsibleWrapper ansible) {
        this.script = script
        this.ansible = ansible
    }

    @Override
    String getName() {
        return script.getScriptName()
    }

    @Override
    void execute(Map<String, Object> context) {
        Map<String, String> env = (Map<String, String>) context.get("env")
        AnsibleScriptContext ansibleScriptContext = (AnsibleScriptContext) context.get("ansible")
        ansible.play(script.getBaseDirectory(),
                ansibleScriptContext.getUser(),
                ansibleScriptContext.getSshKey(),
                ansibleScriptContext.getInventoryFile(),
                env,
                script.getScriptFileLocation().get()
        )
    }

}
