package org.s4s0l.shathel.commons.remoteswarm.downloader

import org.s4s0l.shathel.commons.core.Parameters
import org.s4s0l.shathel.commons.core.dependencies.StackLocator
import org.s4s0l.shathel.commons.git.GitDownloader
import org.s4s0l.shathel.commons.utils.Utils

/**
 * @author Marcin Wielgus
 */
class GitEnvironmentPackageDownloader extends GitDownloader implements EnvironmentPackageDownloader{
    public static
    final String SHATHEL_GIT_DEFAULT_VERSION = "shathel.solution.git_env_default_version"
    public static
    final String SHATHEL_GIT_DEFAULT_GROUP = "shathel.solution.git_env_default_group"
    public static final String DEFAULT_GROUP = "github.com/s4s0l/shathel-envs"
    private final Parameters parameters

    GitEnvironmentPackageDownloader(Parameters parameters) {
        this.parameters = parameters
    }

    @Override
    String getDefaultGroup() {
        return parameters.getParameter(SHATHEL_GIT_DEFAULT_GROUP).orElse(DEFAULT_GROUP)
    }

    @Override
    String getDefaultVersion() {
        return parameters.getParameter(SHATHEL_GIT_DEFAULT_VERSION).orElseGet {
            Utils.getShathelVersion()
        }
    }

    @Override
    Optional<File> download(StackLocator locator, File directory, boolean forceful) {
        def ret = super.download(locator, directory, forceful)
        if (ret.isPresent()) {
            def stackFile = new File(ret.get(), "shthl-env.yml")
            def replaced = stackFile.text.replace("\$version", resolveReference(locator).get().version)
            stackFile.text = replaced
        }
        return ret
    }
}
