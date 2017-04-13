package org.s4s0l.shathel.commons.scripts.vaagrant

import org.s4s0l.shathel.commons.utils.ExecWrapper
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * @author Marcin Wielgus
 */
class VagrantWrapper {
    private static
    final Logger LOGGER = LoggerFactory.getLogger(VagrantWrapper.class);
    final ExecWrapper exec;

    VagrantWrapper(String command) {
        exec = new ExecWrapper(LOGGER, command, [:])
    }


    String up(File workingDir, Map<String, String> envs) {
        return exec.executeForOutput(null, workingDir, envs, "up")
    }
}
