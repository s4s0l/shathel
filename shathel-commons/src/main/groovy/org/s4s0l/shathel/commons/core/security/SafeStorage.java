package org.s4s0l.shathel.commons.core.security;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Optional;

/**
 * @author Marcin Wielgus
 */
public interface SafeStorage {
    void readFile(String key, File copyTo);

    void writeFile(String key, File f);

    Optional<String> readValue(String key);

    void writeValue(String key, String value);

    OutputStream outputStream(String key);

    Optional<InputStream> inputStream(String key);
}
