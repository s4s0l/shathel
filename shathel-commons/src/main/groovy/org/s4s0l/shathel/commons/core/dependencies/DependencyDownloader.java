package org.s4s0l.shathel.commons.core.dependencies;

import org.s4s0l.shathel.commons.core.stack.StackReference;
import org.s4s0l.shathel.commons.utils.ExtensionInterface;

import java.io.File;

/**
 * @author Matcin Wielgus
 */
public interface DependencyDownloader extends ExtensionInterface {
    void download(StackReference reference, File directory);
}
