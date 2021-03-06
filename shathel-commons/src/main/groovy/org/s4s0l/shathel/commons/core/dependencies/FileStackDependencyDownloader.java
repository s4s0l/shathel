package org.s4s0l.shathel.commons.core.dependencies;

import org.s4s0l.shathel.commons.core.ParameterProvider;
import org.s4s0l.shathel.commons.core.model.StackFileModel;
import org.s4s0l.shathel.commons.core.stack.StackReference;
import org.s4s0l.shathel.commons.utils.Utils;
import org.slf4j.Logger;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * @author Marcin Wielgus
 */
public class FileStackDependencyDownloader extends FileDownloader implements StackDependencyDownloader {
    public static final String SHATHEL_FILE_DEFAULT_VERSION = "shathel.solution.file_default_version";
    private static final String SHATHEL_FILE_DEFAULT_GROUP = "shathel.solution.file_default_group";
    public static final String SHATHEL_FILE_BASE_DIR = "shathel.solution.file_base_dir";
    public static final String SHATHEL_FILE_IGNORE_VERSION = "shathel.solution.file_ignore_versions";
    private static final Logger LOGGER = getLogger(FileStackDependencyDownloader.class);
    private final ParameterProvider params;

    public FileStackDependencyDownloader(ParameterProvider params) {
        this.params = params;
    }

    private boolean isIgnoreVersions() {
        return params.getParameter(SHATHEL_FILE_IGNORE_VERSION).orElse("false").equalsIgnoreCase("true");
    }

    @Override
    protected Optional<File> searchAsReference(StackReference stackReference) {
        Optional<File> search = super.searchAsReference(stackReference);
        if (search.isPresent()) {
            StackFileModel model = StackFileModel.load(new File(search.get(), "shthl-stack.yml"));
            StackReference foundStack = new StackReference(model.getGav());

            //we found but it has wrong version
            if (!isIgnoreVersions() && !foundStack.getVersion().equals(stackReference.getVersion())) {
                if (!Arrays.asList("$version", "UNKNOWN", "DEVELOPER-SNAPSHOT").contains(foundStack.getVersion())) {
                    LOGGER.warn("{} found stack in {}, but in different version, will not pick it up", getClass().getSimpleName(), search.get().getAbsolutePath());
                    search = Optional.empty();
                }
            }
        }
        return search;
    }

    @Override
    protected Optional<File> verifyFile(File locationFile) {
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

    @Override
    protected List<File> getBaseSearchPath() {
        String paths = params.getParameter(SHATHEL_FILE_BASE_DIR).orElse(".");
        return Arrays.stream(paths.split(",")).filter(it -> !it.trim().isEmpty()).map(File::new).collect(Collectors.toList());
    }

    @Override
    protected String getDefaultVersion() {
        return params.getParameter(SHATHEL_FILE_DEFAULT_VERSION).orElseGet(Utils::getShathelVersion);
    }

    @Override
    protected String getDefaultGroup() {
        return params.getParameter(SHATHEL_FILE_DEFAULT_GROUP).orElse(DEFAULT_GROUP);
    }

}
