package org.s4s0l.shathel.commons.utils

import org.slf4j.Logger

/**
 * @author Matcin Wielgus
 */
class ExecWrapper {
    final Logger LOGGER;
    final String command;
    final Map<String, String> environment;

    ExecWrapper(Logger LOGGER, String command, Map<String, String> environment = [:]) {
        this.LOGGER = LOGGER
        this.command = command
        this.environment = environment;
    }

    int executeForExitValue(File dir = new File("."), Map<String, String> env = [:], boolean logOutput = false, String args) {
        executeForExitValue(dir, env, logOutput, args.split(' '))
    }

    int executeForExitValue(File dir = new File("."), Map<String, String> env = [:], boolean logOutput = false, String... args) {
        StringBuilder sb = new StringBuilder()
        List<?> flatten = fix(args)
        LOGGER.debug("Running ${flatten.join(",")}")
        Process process = createProcess(flatten, dir, env)
        process.inputStream.eachLine {
            LOGGER.debug("output:  [$it]")
            sb.append(it).append("\n")
        }
        process.waitFor();
        if (process.exitValue() != 0 && logOutput) {
            LOGGER.error("Command failed with output:\n" + sb.toString().trim())
            throw new Exception("Failed")
        }
        return process.exitValue()
    }

    String executeForOutput(File dir = new File("."), Map<String, String> env = [:], String args) {
        executeForOutput(dir, env, args.split(' '))
    }

    String executeForOutput(File dir = new File("."), Map<String, String> env = [:], String... args) {
        StringBuilder sb = new StringBuilder()
        List<?> flatten = fix(args)
        LOGGER.debug("Running ${flatten.join(",")}")
        Process process = createProcess(flatten, dir, env)
        process.inputStream.eachLine {
            LOGGER.debug("output:  [$it]")
            sb.append(it).append("\n")
        }
        process.waitFor();
        if (process.exitValue() == 0) {
            return sb.toString().trim();
        } else {
            LOGGER.error("Command [${flatten.join(" ")}] failed with output:\n" + sb.toString().trim())
            throw new Exception("Failed")
        }
    }

    private List<?> fix(String... args) {
        def flatten = ([] << command.split("\\s") << args).flatten().findAll {
            "" != it.trim()
        }
        flatten
    }

    private Process createProcess(List<String> command, File dir, Map<String, String> env = [:]) {
        ProcessBuilder builder = new ProcessBuilder(command)
                .directory(dir)
                .redirectErrorStream(true)
        builder.environment() << environment << env
        return builder.start()
    }
}
