package org.s4s0l.shathel.commons.scripts.packer

import org.s4s0l.shathel.commons.utils.ExecWrapper
import org.s4s0l.shathel.commons.utils.ExecutableResults
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * @author Marcin Wielgus
 */
class PackerWrapper {
    private static
    final Logger LOGGER = LoggerFactory.getLogger(PackerWrapper.class);
    private final ExecWrapper exec

    PackerWrapper(String command) {
        exec = new ExecWrapper(LOGGER, command, [:])
        exec.acceptedExitCodes.add(1)
    }


    ExecutableResults run(File workingDir, String command, Map<String, String> envs) {
        return exec.execute(workingDir, envs, command)
    }
}
