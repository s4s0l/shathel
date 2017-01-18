package org.s4s0l.shathel.commons.filestorage;

import org.s4s0l.shathel.commons.core.Parameters;
import org.s4s0l.shathel.commons.core.storage.Storage;

import java.io.File;

/**
 * @author Matcin Wielgus
 */
public class FileStorageProvider implements org.s4s0l.shathel.commons.core.storage.StorageProvider {
    private final Parameters parameters;

    public FileStorageProvider(Parameters parameters) {
        this.parameters = parameters;
    }

    @Override
    public Storage getStorage(File directory) {
        return new FileStorage(directory, parameters);
    }
}
