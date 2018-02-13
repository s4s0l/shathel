package org.s4s0l.shathel.commons.remoteswarm.downloader

import org.s4s0l.shathel.commons.core.dependencies.StackLocator
import org.s4s0l.shathel.commons.utils.ExtensionInterface

/**
 * @author Marcin Wielgus
 */
interface EnvironmentPackageDownloader extends ExtensionInterface {
    Optional<File> download(StackLocator reference, File directory, boolean forceful)
}
