package org.s4s0l.shathel.commons.core.dependencies;

import org.s4s0l.shathel.commons.utils.ExtensionInterface;

import java.io.File;
import java.util.Optional;

/**
 * @author Marcin Wielgus
 */
public interface StackDependencyDownloader extends ExtensionInterface {

    Optional<File> download(StackLocator reference, File directory, boolean forceful);
}
