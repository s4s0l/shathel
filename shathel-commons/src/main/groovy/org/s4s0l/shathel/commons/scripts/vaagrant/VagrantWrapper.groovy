package org.s4s0l.shathel.commons.scripts.vaagrant

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
class VagrantWrapper {
    private static
    final Logger LOGGER = LoggerFactory.getLogger(VagrantWrapper.class);
    final ExecWrapper exec;

    VagrantWrapper(String command) {
        exec = new ExecWrapper(LOGGER, command, [:])
    }

    Map<String, String> status(File workingDir, Map<String, String> envs) {
        String ret = exec.executeForOutput(null, workingDir, envs, "status")
        Map<String, String> statuses = [:]
        def x = ret =~ /(?m)^[^\s]+\s+[^\n\(]+ \([^\s]+\)$/
        while (x.find()) {
            def match = x.group() =~ /([^\s]+)\s+([^\n\(]+) \([^\s]+\)/
            match.find()
            statuses.putAll([(match.group(1)): match.group(2)])
        }
        return statuses
    }

    String run(File workingDir, Map<String, String> envs, String commands) {
        return exec.executeForOutput(null, workingDir, envs, commands)
    }

    String up(File workingDir, Map<String, String> envs) {
        return exec.executeForOutput(null, workingDir, envs, "up")
    }

    String halt(File workingDir, Map<String, String> envs) {
        return exec.executeForOutput(null, workingDir, envs, "halt")
    }

    String suspend(File workingDir, Map<String, String> envs) {
        return exec.executeForOutput(null, workingDir, envs, "suspend")
    }

    String resume(File workingDir, Map<String, String> envs) {
        return exec.executeForOutput(null, workingDir, envs, "resume")
    }

    String destroy(File workingDir, Map<String, String> envs) {
        return exec.executeForOutput(null, workingDir, envs, "destroy -f")
    }
}
