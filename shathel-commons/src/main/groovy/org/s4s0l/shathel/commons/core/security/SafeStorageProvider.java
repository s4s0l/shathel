package org.s4s0l.shathel.commons.core.security;

import org.s4s0l.shathel.commons.utils.ExtensionInterface;

import java.io.File;

/**
 * @author Marcin Wielgus
 */
public interface SafeStorageProvider extends ExtensionInterface {

    SafeStorage getSafeStorage(File s, String name);
}
