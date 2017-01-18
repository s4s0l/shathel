package org.s4s0l.shathel.commons.core.storage;

import org.s4s0l.shathel.commons.utils.ExtensionInterface;

import java.io.File;

/**
 * @author Matcin Wielgus
 */
public interface StorageProvider extends ExtensionInterface {
    Storage getStorage( File directory);
}
