package org.s4s0l.shathel.commons.scripts.vaagrant

import org.s4s0l.shathel.commons.scripts.NamedExecutable
import org.s4s0l.shathel.commons.scripts.TypedScript
import org.s4s0l.shathel.commons.scripts.ansible.AnsibleScriptContext

/**
 * @author Marcin Wielgus
 */
class VagrantExecutable implements NamedExecutable {
    private final TypedScript script
    private final VagrantWrapper vagrant

    VagrantExecutable(TypedScript script, VagrantWrapper vagrant) {
        this.script = script
        this.vagrant = vagrant
    }

    @Override
    String getName() {
        return script.getScriptName()
    }

    @Override
    void execute(Map<String, Object> context) {
        Map<String, String> env = (Map<String, String>) context.get("env")
        vagrant.up(script.getBaseDirectory(), env)
    }

}
