package org.s4s0l.shathel.commons.scripts.ansible

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import org.s4s0l.shathel.commons.core.environment.EnvironmentContext
import org.s4s0l.shathel.commons.utils.ExecutableResults
import org.s4s0l.shathel.commons.scripts.NamedExecutable
import org.s4s0l.shathel.commons.scripts.TypedScript
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * @author Marcin Wielgus
 */
@TypeChecked
@CompileStatic
class AnsibleExecutable implements NamedExecutable {
    private static
    final Logger LOGGER = LoggerFactory.getLogger(AnsibleExecutable.class)
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
        File scriptFile = script.getScriptFileLocation().orElseThrow {
            new RuntimeException("Ansible scripts do not support inlining! Or file missing, consult logs.")
        }
        AnsibleScriptContext ansibleScriptContext = (AnsibleScriptContext) context.get("ansible")
        if (ansibleScriptContext.disabled) {
            LOGGER.warn("Ansible script is disabled: {}.", ansibleScriptContext.disabledMessage.get())
            LOGGER.warn("Ansible disabled script was: {}", scriptFile.text);
            return
        }

        Map<String, String> env = (Map<String, String>) context.get("env")
        EnvironmentContext econtext = (EnvironmentContext) context.get("context")
        env.putAll([
                "ANSIBLE_HOST_KEY_CHECKING"    : "False",
                "ANSIBLE_NOCOWS"               : "1",
                "ANSIBLE_RETRY_FILES_SAVE_PATH": econtext.getTempDirectory().absolutePath,
        ])
        if (ansibleScriptContext == null) {
            throw new RuntimeException("Unable to find ansible script context! Ansible unsupported in this context?")
        }
        def extraVarsFile = new File(econtext.tempDirectory, "ansible-extra-vars.json")
        try {

            extraVarsFile.text = "{" + env.findAll {
                //sometime secret values are files, that may be for eg jsons itself
                //this is a lame workaround
                !it.key.toLowerCase().endsWith("_secret_value") && !it.key.toLowerCase().contains("safepassword")
            }
            .collect {
                "\t\"${it.key.toLowerCase()}\":\"${it.value}\""
            }.join(",\n") + "}"
            extraVarsFile.deleteOnExit()
            def out = ansible.play(script.getBaseDirectory(),
                    ansibleScriptContext,
                    env,
                    extraVarsFile,
                    scriptFile
            )
            (context.get("result") as ExecutableResults).output = out
        } finally {
            if (extraVarsFile.exists()) {
                //todo: some parameter to preserve it would be nice
                extraVarsFile.delete()
            }
        }
    }

}
