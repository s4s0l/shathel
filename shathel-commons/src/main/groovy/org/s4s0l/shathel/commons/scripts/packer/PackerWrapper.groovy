package org.s4s0l.shathel.commons.scripts.packer

import org.s4s0l.shathel.commons.utils.ExecWrapper
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
    }


    String run(File workingDir, String command, Map<String, String> envs) {
        return exec.executeForOutput(workingDir, envs, command)
    }
}
