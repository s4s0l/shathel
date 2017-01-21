package org.s4s0l.shathel.commons.core.security;

import org.s4s0l.shathel.commons.core.storage.Storage;
import org.s4s0l.shathel.commons.utils.ExtensionInterface;

/**
 * @author Matcin Wielgus
 */
public interface SafeStorageProvider extends ExtensionInterface {

    SafeStorage getSafeStorage(Storage s, String name);
}
