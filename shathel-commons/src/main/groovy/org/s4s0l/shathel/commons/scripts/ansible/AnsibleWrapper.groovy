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

    String play(File workingDir, AnsibleScriptContext asc,
                Map<String, String> envs, File extraVars, File playbook) {
        asc.customize(envs)
        Map<String, String> args = [
                "timeout"   : "360",
                "extra-vars": "@${extraVars.absolutePath}".toString()
        ]
        args.putAll(asc.arguments)
        String fullArgs = args.collect { "--${it.key}=${it.value}" }.join(" ")
        return exec.executeForOutput(null, workingDir, envs, "$fullArgs ${playbook.absolutePath}".toString())
    }
}
