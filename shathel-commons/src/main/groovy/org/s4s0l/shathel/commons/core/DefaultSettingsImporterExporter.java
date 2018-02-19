package org.s4s0l.shathel.commons.core;

import org.s4s0l.shathel.commons.utils.IoUtils;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author Marcin Wielgus
 */
public class DefaultSettingsImporterExporter implements SettingsImporterExporter {
    @Override
    public void saveSettings(File dockerMachineSettingsFolder, OutputStream outputStream) {
        IoUtils.zipIt(dockerMachineSettingsFolder, outputStream);
    }

    @Override
    public void loadSettings(InputStream is, File destinationDirectory) {
        IoUtils.unZipIt(is, destinationDirectory);
        IoUtils.setPerm0600Recursive(destinationDirectory);
    }
}
