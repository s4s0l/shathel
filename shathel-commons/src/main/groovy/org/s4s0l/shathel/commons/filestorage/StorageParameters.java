package org.s4s0l.shathel.commons.filestorage;

import org.s4s0l.shathel.commons.core.Parameters;

import java.io.File;
import java.util.Optional;

/**
 * @author Matcin Wielgus
 */
public class StorageParameters {

    private final Parameters parameters;
    private final File rootFile;

    public File getRootFile() {
        return rootFile;
    }

    public Parameters getParameters() {
        return parameters;
    }

    public StorageParameters(Parameters parameters, File rootFile) {
        this.parameters = parameters;
        this.rootFile = rootFile;
    }

    public File getTemporaryDirectory(String key) {
        return get(Optional.of("tmp"), key, Optional.of("dir"));
    }

    public File getPersistedDirectory(String key) {
        return get(Optional.of("data"), key, Optional.of("dir"));
    }

    public File getWorkDirectory(String key) {
        return get(Optional.of("work"), key, Optional.of("dir"));
    }

    public File getTemporaryFile(String key) {
        return get(Optional.of("tmp"), key, Optional.of("file"));
    }

    public File getPersistedFile(String key) {
        return get(Optional.of("data"), key, Optional.of("file"));
    }

    public File get(Optional<String> prefix, String key, Optional<String> type) {
        return parameters.getParameter(
                "shathel.storage" +
                        prefix.map(p -> "." + p).orElse("")
                        + "." + key
                        + type.map(t -> "." + t).orElse(""))
                .map(v -> new File(v))
                .map(f -> f.isAbsolute() ? f : new File(rootFile, f.getPath()))
                .orElse(new File(rootFile,
                        prefix.map(p -> "./" + p).orElse(".") + "/" + key));
    }


}
