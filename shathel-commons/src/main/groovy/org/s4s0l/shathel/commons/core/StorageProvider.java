package org.s4s0l.shathel.commons.core;

import java.io.File;

/**
 * @author Matcin Wielgus
 */
public interface StorageProvider {
    Storage getStorage(File directory);
}
