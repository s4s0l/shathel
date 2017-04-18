package org.s4s0l.shathel.commons.scripts.ansible

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import org.s4s0l.shathel.commons.core.environment.EnvironmentContext
import org.s4s0l.shathel.commons.scripts.ExecutableResults
import org.s4s0l.shathel.commons.scripts.NamedExecutable
import org.s4s0l.shathel.commons.scripts.TypedScript

/**
 * @author Marcin Wielgus
 */
@TypeChecked
@CompileStatic
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
        if (context.get("result") == null) {
            context.put("result", new ExecutableResults())
        }
        Map<String, String> env = (Map<String, String>) context.get("env")
        env.putAll([
                "ANSIBLE_HOST_KEY_CHECKING": "False",
                "ANSIBLE_NOCOWS"           : "1",
        ])
        AnsibleScriptContext ansibleScriptContext = (AnsibleScriptContext) context.get("ansible")
        EnvironmentContext econtext = (EnvironmentContext) context.get("context")

        def extraVarsFile = new File(econtext.tempDirectory, "ansible-extra-vars.json")
        try {

            extraVarsFile.text = "{" + env.collect {
                "\t\"${it.key.toLowerCase()}\":\"${it.value}\""
            }.join(",\n") + "}"

            def out = ansible.play(script.getBaseDirectory(),
                    ansibleScriptContext.getUser(),
                    ansibleScriptContext.getSshKey(),
                    ansibleScriptContext.getInventoryFile(),
                    env,
                    extraVarsFile,
                    script.getScriptFileLocation().get()
            )
            (context.get("result") as ExecutableResults).output = out
        } finally {
            if (extraVarsFile.exists()) {
                extraVarsFile.delete()
            }
        }
    }

}
