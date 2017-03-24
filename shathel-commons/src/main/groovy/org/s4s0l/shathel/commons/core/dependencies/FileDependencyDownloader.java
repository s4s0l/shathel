package org.s4s0l.shathel.commons.core.dependencies;

import org.apache.commons.io.FileUtils;
import org.s4s0l.shathel.commons.core.ParameterProvider;
import org.s4s0l.shathel.commons.core.model.GavUtils;
import org.s4s0l.shathel.commons.core.model.SolutionFileModel;
import org.s4s0l.shathel.commons.core.model.StackFileModel;
import org.s4s0l.shathel.commons.core.stack.StackReference;
import org.s4s0l.shathel.commons.utils.IoUtils;
import org.s4s0l.shathel.commons.utils.Utils;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * @author Marcin Wielgus
 */
public class FileDependencyDownloader implements DependencyDownloader {
    public static final String SHATHEL_FILE_DEFAULT_VERSION = "shathel.solution.file_default_version";
    public static final String SHATHEL_FILE_DEFAULT_GROUP = "shathel.solution.file_default_group";
    public static final String DEFAULT_GROUP = "org.s4s0l.shathel";
    public static final String SHATHEL_FILE_BASE_DIR = "shathel.file.base_dir";
    private final ParameterProvider params;

    public FileDependencyDownloader(ParameterProvider params) {
        this.params = params;
    }

    File getBaseSearchPath() {
        return new File(params.getParameter(SHATHEL_FILE_BASE_DIR).orElse("."));
    }

    @Override
    public Optional<File> download(StackLocator reference, File directory, boolean forceful) {
        Optional<File> search = search(reference, directory, forceful);
        return search;
    }

    private Optional<StackReference> getReference(StackLocator locator) {
        String group = params.getParameter(SHATHEL_FILE_DEFAULT_GROUP).orElse(DEFAULT_GROUP);
        String version = params.getParameter(SHATHEL_FILE_DEFAULT_VERSION).orElseGet(() -> Utils.getShathelVersion());
        return new ReferenceResolver(group, version).resolve(locator);
    }

    private Optional<File> search(StackLocator locator, File directory, boolean forceful) {
        Optional<StackReference> reference = getReference(locator);
        if (reference.isPresent()) {
            Optional<File> file = searchAsReference(reference.get());
            if (!file.isPresent()) {
                file = searchAsLocalMavenRepo(reference.get(), directory, forceful);
            }
            return file;
        }
        return searchAsFile(new File(locator.getLocation()));
    }

    private Optional<File> searchAsReference(StackReference stackReference) {
        Optional<File> search = searchAsFile(new File(stackReference.getName() + "-" + stackReference.getVersion()));
        if (!search.isPresent()) {
            search = searchAsFile(new File(stackReference.getName() + "-" + stackReference.getVersion() + "-shathel"));
        }
        if (!search.isPresent()) {
            search = searchAsFile(new File(stackReference.getName()));
        }
        if (search.isPresent()) {
            StackFileModel model = StackFileModel.load(new File(search.get(), "shthl-stack.yml"));
            StackReference foundStack = new StackReference(model.getGav());

            //we found but it has wrong version
            if (!foundStack.getVersion().equals(stackReference.getVersion())) {
                if (!foundStack.getVersion().equals("$version")) {
                    LOGGER.warn("{} found stack in {}, but in different version, will not pick it up", getClass().getSimpleName(), search.get().getAbsolutePath());
                    search = Optional.empty();
                }
            }
        }
        return search;
    }

    private static final Logger LOGGER = getLogger(FileDependencyDownloader.class);

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

        if (locationFile.exists()) {
            if (locationFile.isDirectory()) {
                if (new File(locationFile, "shthl-stack.yml").isFile()) {
                    return Optional.of(locationFile);
                }
            } else if (locationFile.isFile()) {
                if (locationFile.getName().equals("shthl-stack.yml")) {
                    return Optional.of(locationFile.getParentFile());
                }
            }
        }
        return Optional.empty();
    }
}
