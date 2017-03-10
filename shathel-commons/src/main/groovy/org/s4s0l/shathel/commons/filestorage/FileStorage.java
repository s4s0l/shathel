package org.s4s0l.shathel.commons.filestorage;

import org.s4s0l.shathel.commons.core.ParameterProvider;
import org.s4s0l.shathel.commons.core.storage.Storage;

import java.io.File;

/**
 * @author Marcin Wielgus
 */
public class FileStorage implements Storage {

    private final File root;

    public FileStorage(File root) {
        root.mkdirs();
        this.root = root;
    }

    private File get(ParameterProvider parameterProvider, String type, File defaultDir) {
        return parameterProvider.getParameter(type + "Dir")
                .map(v -> new File(v))
                .map(f -> f.isAbsolute() ? f : new File(root, f.getPath()))
                .orElse(defaultDir);
    }

    private File get(ParameterProvider parameterProvider, String env, String type) {
        return get(parameterProvider,  type, new File(root,
                env + "/" + type));
    }

    @Override
    public File getDependencyCacheDirectory(ParameterProvider parameterProvider, String env) {
        return ensureExists(get(parameterProvider,  "dependencies", new File(root, ".dependency-cache")));
    }

    @Override
    public File getDataDirectory(ParameterProvider parameterProvider, String env) {
        return ensureExists(get(parameterProvider, env, "data"));
    }

    @Override
    public File getSafeDirectory(ParameterProvider parameterProvider, String env) {
        return ensureExists(get(parameterProvider, env, "safe"));
    }

    @Override
    public File getSettingsDirectory(ParameterProvider parameterProvider, String env) {
        return ensureExists(get(parameterProvider, env, "settings"));
    }

    @Override
    public File getEnrichedDirectory(ParameterProvider parameterProvider, String env) {
        return ensureExists(get(parameterProvider, env, "enriched"));
    }

    @Override
    public File getTemptDirectory(ParameterProvider parameterProvider, String env) {
        return ensureExists(get(parameterProvider, env, "temp"));
    }

    private File ensureExists(File f) {
        f.mkdirs();
        return f;
    }

    @Override
    public void verify() {
        File configuration = getConfiguration();

        assertIt(root.exists(), "Root storage directory does not exist.");
        assertIt(configuration.exists(), "Configuration file does not exist.");
        assertIt(configuration.isFile(), "Config file is not a file.");
        assertIt(root.isDirectory(), "Root storage dir is not directory.");
        assertIt(isAncestor(configuration, root), "Root dir should contain config file!");
    }

    private void assertIt(boolean condition, String errorMessage) {
        if (!condition) {
            throw new RuntimeException("Storage verification failed! Reason:" + errorMessage + " ( " + root.getAbsolutePath() + " vs " + getConfiguration().getAbsolutePath() + " ) ");
        }
    }

    private final static boolean isAncestor(File offspring, File ancestor) {
        return offspring.getAbsolutePath().startsWith(ancestor.getAbsolutePath());
    }

    @Override
    public File getConfiguration() {
        return new File(root, "shathel-solution.yml");
    }

}
