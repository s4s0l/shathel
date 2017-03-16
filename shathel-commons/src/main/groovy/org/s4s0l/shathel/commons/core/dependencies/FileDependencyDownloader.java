package org.s4s0l.shathel.commons.core.dependencies;

import org.s4s0l.shathel.commons.core.model.GavUtils;
import org.s4s0l.shathel.commons.core.model.SolutionFileModel;
import org.s4s0l.shathel.commons.core.model.StackFileModel;
import org.s4s0l.shathel.commons.core.stack.StackReference;

import java.io.File;
import java.util.Optional;

/**
 * @author Marcin Wielgus
 */
public class FileDependencyDownloader implements DependencyDownloader {
    private final File baseSearchPath;

    public FileDependencyDownloader(File bbaseSearchPath) {
        this.baseSearchPath = bbaseSearchPath;
    }

    public FileDependencyDownloader() {
        this(new File("."));
    }

    @Override
    public Optional<File> download(StackLocator reference, File directory, boolean forceful) {
        Optional<File> search = search(reference);

        return search;
    }

    private Optional<File> search(StackLocator reference) {
        if (reference.getReference().isPresent()) {
            return searchAsReference(reference);
        }
        return searchAsFile(new File(reference.getLocation()));
    }

    private Optional<File> searchAsReference(StackLocator reference) {
        StackReference stackReference = reference.getReference().get();
        Optional<File> search = searchAsFile(new File(stackReference.getName() + "-" + stackReference.getVersion()));
        if (!search.isPresent()) {
            search = searchAsFile(new File(stackReference.getName() + "-" + stackReference.getVersion() + "-shathel"));
        }
        if (!search.isPresent()) {
            search = searchAsFile(new File(stackReference.getName()));
            if (search.isPresent()) {
                StackFileModel model = StackFileModel.load(new File(search.get(), "shthl-stack.yml"));
                if (!GavUtils.getVersion(model.getGav()).equals(stackReference.getVersion())) {
                    return Optional.empty();
                }
            }
        }
        return search;
    }

    private Optional<File> searchAsFile(File locationFile) {

        if (!locationFile.isAbsolute()) {
            locationFile = new File(baseSearchPath, locationFile.getPath());
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
