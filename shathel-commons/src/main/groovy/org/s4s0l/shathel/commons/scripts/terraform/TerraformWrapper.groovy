package org.s4s0l.shathel.commons.scripts.terraform

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
class TerraformWrapper {
    private static
    final Logger LOGGER = LoggerFactory.getLogger(TerraformWrapper.class);
    private final ExecWrapper exec

    TerraformWrapper(String command) {
        exec = new ExecWrapper(LOGGER, command, [:])
    }

    String apply(File workingDir, File stateFile, File tfFilesDir, Map<String, String> envs) {
        Map<String, String> tfEnvs = toTerraformEnvs(envs)
        return exec.executeForOutput(null, workingDir, tfEnvs, "apply -no-color -state ${stateFile.absolutePath} ${tfFilesDir.absolutePath}")
    }


    String run(File workingDir, File stateFile, File tfFilesDir, Map<String, String> envs, String command) {
        Map<String, String> tfEnvs = toTerraformEnvs(envs)
        return exec.executeForOutput(null, workingDir, tfEnvs, "${command} -state ${stateFile.absolutePath} ${tfFilesDir.absolutePath}")
    }


    Map<String, String> output(File workingDir, File stateFile, File tfFilesDir, Map<String, String> envs) {
        Map<String, String> tfEnvs = toTerraformEnvs(envs)
        try {
            String output = exec.executeForOutput(null, workingDir, tfEnvs, "output -state=${stateFile.absolutePath}")
            def a = output =~ /(?m)^([^\s]+) = (.+)$/
            Map<String, String> ret = [:]
            while (a.find()) {
                ret.putAll([(a.group(1).toUpperCase()): a.group(2)])
            }
            return ret
        } catch (Exception e) {
            LOGGER.warn("Terraform has no outputs!", e)
            return [:]
        }
    }

    Map<String, Integer> plan(File workingDir, File stateFile, File tfFilesDir, Map<String, String> envs) {
        Map<String, String> tfEnvs = toTerraformEnvs(envs)
        String planRes = exec.executeForOutput(null, workingDir, tfEnvs, "plan -no-color -state ${stateFile.absolutePath} ${tfFilesDir.absolutePath}")
        if (planRes.contains("No changes. Infrastructure is up-to-date.")) {
            return [add: 0, change: 0, destroy: 0, allChanges: 0]
        } else {
            String lastLine = planRes.readLines().last()
            def match = lastLine =~ /Plan: (\d+) to add, (\d+) to change, (\d+) to destroy./
            match.find()
            int add = Integer.parseInt(match.group(1))
            int change = Integer.parseInt(match.group(2))
            int destroy = Integer.parseInt(match.group(3))
            return [add: add, change: change, destroy: destroy, allChanges: add + change + destroy]
        }
    }

    String destroy(File workingDir, File stateFile, File tfFilesDir, Map<String, String> envs) {
        Map<String, String> tfEnvs = toTerraformEnvs(envs)
        return exec.executeForOutput(null, workingDir, tfEnvs, "destroy -force -no-color -state ${stateFile.absolutePath} ${tfFilesDir.absolutePath}")
    }

    private Map<String, String> toTerraformEnvs(Map<String, String> envs) {
        Map<String, String> tfEnvs = [:]
        tfEnvs.putAll(envs)
        tfEnvs.putAll((Map<String, String>) envs.findAll {
            !it.key.startsWith("TF_VAR_")
        }
        .collectEntries {
            [
                    ("TF_VAR_${it.key}")              : it.value,
                    ("TF_VAR_${it.key.toLowerCase()}"): it.value,
                    ("TF_VAR_${it.key.toUpperCase()}"): it.value
            ]
        })
        return tfEnvs
    }

}
