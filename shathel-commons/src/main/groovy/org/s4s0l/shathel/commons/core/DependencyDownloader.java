package org.s4s0l.shathel.commons.core;

import java.io.File;

/**
 * @author Matcin Wielgus
 */
public interface DependencyDownloader {
    void download(StackReference reference, File directory);
}
