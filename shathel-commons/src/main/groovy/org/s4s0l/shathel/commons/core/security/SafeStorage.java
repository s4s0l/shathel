package org.s4s0l.shathel.commons.core.security;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Optional;

/**
 * @author Matcin Wielgus
 */
public class SafeStorage {
    private final File rootDir;
    private final char[] masterPassword;


    public SafeStorage(File rootDir, char[] masterPassword) {
        this.rootDir = rootDir;
        this.masterPassword = masterPassword;
    }

    public Optional<InputStream> readFile(String key) {

    }

    public OutputStream writeFile(String ket) {

    }

    public Optional<String> readValue(String key) {

    }

    public String writeValue(String key) {

    }
}
