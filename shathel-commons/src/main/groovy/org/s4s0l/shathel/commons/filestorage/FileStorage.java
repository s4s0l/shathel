package org.s4s0l.shathel.commons.filestorage;

import org.apache.commons.io.FileUtils;
import org.s4s0l.shathel.commons.core.Parameters;
import org.s4s0l.shathel.commons.core.storage.Storage;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

/**
 * @author Matcin Wielgus
 */
public class FileStorage implements Storage {
    private final StorageParameters storageParameters;

    public FileStorage(File root, Parameters parameters) {
        root.mkdirs();
        storageParameters = new StorageParameters(parameters, root);
    }


    @Override
    public File getTemporaryDirectory(String name) {
        File temporaryDirectory = storageParameters.getTemporaryDirectory(name);
        temporaryDirectory.mkdirs();
        return temporaryDirectory;
    }

    @Override
    public File getWorkDirectory(String name) {
        File temporaryDirectory = storageParameters.getWorkDirectory(name);
        temporaryDirectory.mkdirs();
        return temporaryDirectory;
    }

    @Override
    public File getPersistedDirectory(String name) {
        File persistedDirectory = storageParameters.getPersistedDirectory(name);
        persistedDirectory.mkdirs();
        return persistedDirectory;
    }

    @Override
    public void verify() {
        //todo split it with meaningfull messages
        if (!storageParameters.getRootFile().exists()
                || !getConfiguration().exists()
                || !getConfiguration().isFile()
                || !storageParameters.getRootFile().isDirectory()
                || !isAncestor(getConfiguration(), storageParameters.getRootFile())) {
            throw new RuntimeException("Storage verification failed!");
        }
    }

    private final static boolean isAncestor(File offspring, File ancestor) {
        return offspring.getAbsolutePath().startsWith(ancestor.getAbsolutePath());
    }

    @Override
    public File getConfiguration() {
        return storageParameters.get(Optional.empty(), "shathel-solution.yml", Optional.empty());
    }

    @Override
    public boolean isModified() {
        return true;
    }

    @Override
    public void save() {

    }
}
