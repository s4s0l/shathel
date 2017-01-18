package org.s4s0l.shathel.commons.filestorage;

import org.s4s0l.shathel.commons.core.Parameters;
import org.s4s0l.shathel.commons.core.storage.Storage;

import java.io.File;

/**
 * @author Matcin Wielgus
 */
public class FileStorage implements Storage {
    private final File root;
    private final Parameters parameters;

    public FileStorage(File root, Parameters parameters) {
        this.root = root;
        this.parameters = parameters;
    }

    @Override
    public File getDependenciesDir() {
        String dependencies = parameters.getParameter("storage.dependencies.dir",
                new File(root, "dependencies").getAbsolutePath());
        return new File(dependencies);
    }

    @Override
    public File getExecutionDir() {
        String execution = parameters.getParameter("storage.execution.dir",
                new File(root, "execution").getAbsolutePath());
        return new File(execution);

    }

    @Override
    public File getMountsDir() {
        String execution = parameters.getParameter("storage.mounts.dir",
                new File(root, "mounts").getAbsolutePath());
        return new File(execution);
    }
}
