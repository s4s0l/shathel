package org.s4s0l.shathel.commons.remoteswarm.downloader

import org.s4s0l.shathel.commons.core.ParameterProvider
import org.s4s0l.shathel.commons.core.dependencies.FileDownloader
import org.s4s0l.shathel.commons.utils.Utils
import org.slf4j.Logger

import static org.slf4j.LoggerFactory.getLogger

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
    private static
    final Logger LOGGER = getLogger(FileEnvironmentPackageDownloader.class)
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
    protected File getBaseSearchPath() {
        return new File(params.getParameter(SHATHEL_FILE_BASE_DIR).orElse("."))
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