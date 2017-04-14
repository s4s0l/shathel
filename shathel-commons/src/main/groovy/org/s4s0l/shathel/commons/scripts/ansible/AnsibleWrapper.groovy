package org.s4s0l.shathel.commons.scripts.ansible

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import org.s4s0l.shathel.commons.utils.ExecWrapper
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * @author Marcin Wielgus
 */
@TypeChecked
@CompileStatic
class AnsibleWrapper {
    private static
    final Logger LOGGER = LoggerFactory.getLogger(AnsibleWrapper.class);
    final ExecWrapper exec;

    AnsibleWrapper(String command) {
        exec = new ExecWrapper(LOGGER, command, [:])
    }

    String play(File workingDir, String user, File privateKey, File inventoryFile,
                Map<String, String> envs, File playbook) {
        def ansibleExtraVars = envs.collect {
            "${it.key.toLowerCase()}=${it.value}"
        }.join(" ")
        return exec.executeForOutput(null, workingDir, envs,
                "-u", user,
                "--timeout=160",
                "--private-key=${privateKey.absolutePath}".toString(),
                "--inventory-file=${inventoryFile.absolutePath}".toString(),
                "--extra-vars", ansibleExtraVars.toString(),
                "${playbook.absolutePath}".toString())
    }
}
