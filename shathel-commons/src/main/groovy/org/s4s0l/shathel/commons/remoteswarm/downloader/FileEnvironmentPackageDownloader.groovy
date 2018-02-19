package org.s4s0l.shathel.commons.remoteswarm.downloader

import org.s4s0l.shathel.commons.core.ParameterProvider
import org.s4s0l.shathel.commons.core.dependencies.FileDownloader
import org.s4s0l.shathel.commons.utils.Utils

/**
 * @author Marcin Wielgus
 */
class FileEnvironmentPackageDownloader extends FileDownloader implements EnvironmentPackageDownloader {
    public static
    final String SHATHEL_FILE_DEFAULT_VERSION = "shathel.solution.file_env_default_version"
    public static
    final String SHATHEL_FILE_DEFAULT_GROUP = "shathel.solution.file_env_default_group"
    public static
    final String SHATHEL_FILE_BASE_DIR = "shathel.solution.file_env_base_dir"
    private final ParameterProvider params

    FileEnvironmentPackageDownloader(ParameterProvider params) {
        this.params = params
    }


    @Override
    protected Optional<File> verifyFile(File locationFile) {
        if (locationFile.exists()) {
            if (locationFile.isDirectory()) {
                if (new File(locationFile, "shthl-env.yml").isFile()) {
                    return Optional.of(locationFile)
                }
            } else if (locationFile.isFile()) {
                if (locationFile.getName().equals("shthl-env.yml")) {
                    return Optional.of(locationFile.getParentFile())
                }
            }
        }
        return Optional.empty()
    }

    @Override
    protected List<File> getBaseSearchPath() {
        def paths = params.getParameter(SHATHEL_FILE_BASE_DIR).orElse(".")
        paths.split(",").findAll { !it.trim().isEmpty() }.collect {
            new File(it)
        }
    }

    @Override
    protected String getDefaultVersion() {
        return params.getParameter(SHATHEL_FILE_DEFAULT_VERSION).orElseGet {
            Utils.getShathelVersion()
        }
    }

    @Override
    protected String getDefaultGroup() {
        return params.getParameter(SHATHEL_FILE_DEFAULT_GROUP).orElse(DEFAULT_GROUP)
    }

}
