package org.s4s0l.shathel.commons.core;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author Matcin Wielgus
 */
public interface SettingsImporterExporter {
    void saveSettings(File dockerMachineSettingsFolder, OutputStream outputStream);

    void loadSettings(InputStream is, File destinationDirectory);
}
