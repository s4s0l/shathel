package org.s4s0l.shathel.commons.core.stack;

import org.s4s0l.shathel.commons.core.model.ComposeFileModel;

import java.io.File;

/**
 * @author Matcin Wielgus
 */
public class StackResources {
    private final File stackDirectory;

    public StackResources(File stackDirectory) {
        this.stackDirectory = stackDirectory;
    }

    public File getStackDirectory() {
        return stackDirectory;
    }

    public File getComposeFileDirectory() {
        return new File(getStackDirectory(), "stack");
    }

    public ComposeFileModel getComposeFileModel() {
        return ComposeFileModel.load(new File(getStackDirectory(), "stack/docker-compose.yml"));
    }
}
