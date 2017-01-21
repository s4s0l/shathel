package org.s4s0l.shathel.commons.core.storage;

import org.s4s0l.shathel.commons.core.SolutionDescription;

import java.io.File;

/**
 * @author Matcin Wielgus
 */
public interface Storage {

    File getTemporaryDirectory(String name);

    File getWorkDirectory(String name);

    File getPersistedDirectory(String name);

    void verify();

    File getConfiguration();

    boolean isModified();

    void save();


}
