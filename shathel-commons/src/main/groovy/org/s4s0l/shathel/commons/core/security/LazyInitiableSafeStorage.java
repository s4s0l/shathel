package org.s4s0l.shathel.commons.core.security;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Optional;

/**
 * @author Marcin Wielgus
 */
public class LazyInitiableSafeStorage implements SafeStorage {

    private final Supplier<SafeStorage> wrapper;

    public LazyInitiableSafeStorage(Supplier<SafeStorage> wrapper) {
        this.wrapper = Suppliers.memoize(wrapper);
    }


    private SafeStorage get() {
        return wrapper.get();
    }

    @Override
    public void readFile(String key, File copyTo) {
        get().readFile(key, copyTo);
    }

    @Override
    public void writeFile(String key, File f) {
        get().writeFile(key, f);
    }

    @Override
    public Optional<String> readValue(String key) {
        return get().readValue(key);
    }

    @Override
    public void writeValue(String key, String value) {
        get().writeValue(key, value);
    }

    @Override
    public OutputStream outputStream(String key) {
        return get().outputStream(key);
    }

    @Override
    public Optional<InputStream> inputStream(String key) {
        return get().inputStream(key);
    }
}
