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
                Map<String, String> envs,File extraVars,  File playbook) {

        if(asc.sshKey.isPresent()){
            return exec.executeForOutput(null, workingDir, envs,
                    "-u", asc.user,
                    "--timeout=180",
                    "--private-key=${asc.sshKey.get().absolutePath}".toString(),
                    "--inventory-file=${asc.inventoryFile.absolutePath}".toString(),
                    "--extra-vars", "@${extraVars.absolutePath}",
                    "${playbook.absolutePath}".toString())
        }else{
            return exec.executeForOutput(null, workingDir, envs,
                    "-u", asc.user,
                    "--timeout=180",
                    "--inventory-file=${asc.inventoryFile.absolutePath}".toString(),
                    "--extra-vars", "@${extraVars.absolutePath}",
                    "${playbook.absolutePath}".toString())
        }

    }
}
