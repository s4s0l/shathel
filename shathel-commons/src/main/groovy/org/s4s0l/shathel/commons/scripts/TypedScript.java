package org.s4s0l.shathel.commons.scripts;

import java.io.File;
import java.util.Optional;

/**
 * @author Marcin Wielgus
 */
public interface TypedScript {
    String getType();

    String getScriptContents();

    String getScriptName();

    File getBaseDirectory();

    Optional<File> getScriptFileLocation();
}
