package org.s4s0l.shathel.deployer.shell.customization;

import org.apache.commons.io.IOUtils;
import org.springframework.shell.commands.OsOperations;
import org.springframework.shell.support.logging.HandlerUtils;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.logging.Logger;

/**
 * @author Marcin Wielgus
 */

public class OsOperationsImpl {
    private static final Logger LOGGER = HandlerUtils.getLogger(org.springframework.shell.commands.OsOperationsImpl.class);

    public String executeCommand(final String command) throws IOException {
        final File root = new File(".").getAbsoluteFile();
        final Process p = Runtime.getRuntime().exec(command, null, root);
        Reader input = new InputStreamReader(p.getInputStream());
        Reader errors = new InputStreamReader(p.getErrorStream());

        StringBuilder sb = new StringBuilder();
        for (String line : IOUtils.readLines(input)) {
            sb.append(line).append("\n");
        }
        for (String line : IOUtils.readLines(errors)) {
            LOGGER.severe(line);
        }
        p.getOutputStream().close();
        try {
            if (p.waitFor() != 0) {
                throw new RuntimeException("The command '" + command + "' did not complete successfully");
            }
        } catch (final InterruptedException e) {
            throw new IllegalStateException(e);
        }
        return sb.toString();
    }

}
