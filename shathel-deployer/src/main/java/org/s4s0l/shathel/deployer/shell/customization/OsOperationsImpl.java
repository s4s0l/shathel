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

public class OsOperationsImpl implements OsOperations {
    private static final Logger LOGGER = HandlerUtils.getLogger(org.springframework.shell.commands.OsOperationsImpl.class);

    public void executeCommand(final String command) throws IOException {
        final File root = new File(".").getAbsoluteFile();
        final Process p = Runtime.getRuntime().exec(command, null, root);
        Reader input = new InputStreamReader(p.getInputStream());
        Reader errors = new InputStreamReader(p.getErrorStream());

        for (String line : IOUtils.readLines(input)) {
            if (line.startsWith("[ERROR]")) {
                LOGGER.severe(line);
            } else if (line.startsWith("[WARNING]")) {
                LOGGER.warning(line);
            } else {
                LOGGER.info(line);
            }
        }


        for (String line : IOUtils.readLines(errors)) {
            if (line.startsWith("[ERROR]")) {
                LOGGER.severe(line);
            } else if (line.startsWith("[WARNING]")) {
                LOGGER.warning(line);
            } else {
                LOGGER.info(line);
            }
        }


        p.getOutputStream().close();


        try {
            if (p.waitFor() != 0) {
                LOGGER.warning("The command '" + command + "' did not complete successfully");
            }
        } catch (final InterruptedException e) {
            throw new IllegalStateException(e);
        }
    }

}
