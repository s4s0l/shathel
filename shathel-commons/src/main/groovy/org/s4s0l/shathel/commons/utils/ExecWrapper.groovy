package org.s4s0l.shathel.commons.utils

import org.slf4j.Logger

/**
 * @author Matcin Wielgus
 */
class ExecWrapper {
    final Logger LOGGER;
    final String command;

    ExecWrapper(Logger LOGGER, String command) {
        this.LOGGER = LOGGER
        this.command = command
    }

    def executeForExitValue(File dir, String args) {
        executeForExitValue(dir, args.split(" "))
    }

    def executeForExitValue(File dir, String... args) {
        def process = new ProcessBuilder(([command] << args).flatten())
                .directory(dir)
                .redirectErrorStream(true)
                .start()
        process.inputStream.eachLine { LOGGER.debug("$command output:  $it") }
        process.waitFor();
        return process.exitValue()
    }

    def executeForOutput(File dir, String args) {
        executeForOutput(dir, args.split(' '));
    }

    def executeForOutput(File dir, String... args) {
        StringBuilder sb = new StringBuilder()
        def process = new ProcessBuilder(([command] << args).flatten())
                .directory(dir)
                .redirectErrorStream(true)
                .start()
        process.inputStream.eachLine {
            LOGGER.debug("$command output:  $it")
            sb.append(it)
        }
        process.waitFor();
        if (process.exitValue() == 0) {
            return sb.toString();
        } else {
            throw new Exception("Failed")
        }
    }
}
