package org.s4s0l.shathel.commons.core.dependencies;

import org.apache.commons.io.FileUtils;
import org.s4s0l.shathel.commons.core.stack.StackReference;
import org.s4s0l.shathel.commons.utils.IoUtils;
import org.s4s0l.shathel.commons.utils.Utils;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

/**
 * @author Marcin Wielgus
 */
public class FileDownloader {
    public static final String DEFAULT_GROUP = "org.s4s0l.shathel";

    protected Optional<File> verifyFile(File locationFile) {
        if (locationFile.exists()) {
            if (locationFile.isDirectory()) {
                return Optional.of(locationFile);
            } else if (locationFile.isFile()) {
                return Optional.of(locationFile.getParentFile());
            }
        }
        return Optional.empty();
    }


    protected File getBaseSearchPath() {
        return new File(".");
    }

    protected String getDefaultVersion() {
        return Utils.getShathelVersion();
    }

    protected String getDefaultGroup() {
        return DEFAULT_GROUP;
    }


    public Optional<File> download(StackLocator reference, File directory, boolean forceful) {
        return search(reference, directory, forceful);
    }

    protected Optional<StackReference> getReference(StackLocator locator) {
        String group = getDefaultGroup();
        String version = getDefaultVersion();
        return new ReferenceResolver(group, version).resolve(locator);
    }


    private Optional<File> search(StackLocator locator, File directory, boolean forceful) {
        Optional<StackReference> reference = getReference(locator);
        if (reference.isPresent()) {
            Optional<File> file = searchAsReference(reference.get());
            if (!file.isPresent()) {
                file = searchAsLocalMavenRepo(reference.get(), directory, forceful);
            }
            if (file.isPresent()) {
                return file;
            }
        }
        return searchAsFile(new File(locator.getLocation()));
    }

    Optional<File> searchAsReference(StackReference stackReference) {
        Optional<File> search = searchAsFile(new File(stackReference.getName() + "-" + stackReference.getVersion()));
        if (!search.isPresent()) {
            search = searchAsFile(new File(stackReference.getName() + "-" + stackReference.getVersion() + "-shathel"));
        }
        if (!search.isPresent()) {
            search = searchAsFile(new File(stackReference.getName()));
        }

        return search;
    }


    private Optional<File> searchAsLocalMavenRepo(StackReference stackReference, File destination, boolean forceful) {
        String path = stackReference.getGroup().replace(".", "/");
        path = path + "/" + stackReference.getName();
        path = path + "/" + stackReference.getVersion();
        path = path + "/" + stackReference.getName() + "-" + stackReference.getVersion() + "-shathel.zip";
        File f = new File(getBaseSearchPath(), path);
        if (f.exists()) {
            File dest = new File(destination, "files/" + stackReference.getStackDirecctoryName());
            if (dest.exists()) {
                if (!forceful)
                    return Optional.of(dest);
                else {
                    try {
                        FileUtils.deleteDirectory(dest);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
            IoUtils.unZipIt(f, dest);
            return Optional.of(dest);
        }
        return Optional.empty();
    }

    private Optional<File> searchAsFile(File locationFile) {
        if (!locationFile.isAbsolute()) {
            locationFile = new File(getBaseSearchPath(), locationFile.getPath());
        }
        return verifyFile(locationFile);
    }
}
