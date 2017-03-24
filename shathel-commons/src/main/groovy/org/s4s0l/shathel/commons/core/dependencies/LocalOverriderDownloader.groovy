package org.s4s0l.shathel.commons.core.dependencies

import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * @author Marcin Wielgus
 */
class LocalOverriderDownloader implements DependencyDownloader {
    public static final String CURRENT_PROJECT_LOCATION = "--currentProject--"
    private final File stackMappingsDir
    private final File projectStackDir

    LocalOverriderDownloader(File stackMappingsDir, File projectStackDir) {
        this.stackMappingsDir = stackMappingsDir
        this.projectStackDir = projectStackDir
    }
    private static
    final Logger LOGGER = LoggerFactory.getLogger(LocalOverriderDownloader.class);

    @Override
    Optional<File> download(StackLocator locator, File directory, boolean forceful) {
        if (locator.location.startsWith(CURRENT_PROJECT_LOCATION)) {
            return Optional.of(projectStackDir)
        }
        if (!locator.getReference().isPresent()) {
            return Optional.empty()
        }
        def reference = locator.getReference().get();

        def gav = reference.getGav().replaceAll("[^a-zA-Z0-9]", "_")
        def gavFile = new File(stackMappingsDir, gav)
        if (!gavFile.exists()) {
            return Optional.empty()
        }

        def targetFile = new File(gavFile.text)
        if (!targetFile.exists()) {
            LOGGER.warn("${targetFile} does not exist but is pointed in file ${gavFile}, and was needed:(")
        }
        return Optional.of(targetFile)
    }
}