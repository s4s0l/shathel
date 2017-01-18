package org.s4s0l.shathel.commons.core.storage;

import java.io.File;

/**
 * @author Matcin Wielgus
 */
public interface Storage {

    File getDependenciesDir();

    File getExecutionDir();

    File getMountsDir();
}
