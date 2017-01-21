package org.s4s0l.shathel.commons.core.security;

import org.s4s0l.shathel.commons.core.Parameters;
import org.s4s0l.shathel.commons.core.storage.Storage;

/**
 * @author Matcin Wielgus
 */
public class DefaultSafeStorageProvider implements SafeStorageProvider {
    private final Parameters parameters;

    public DefaultSafeStorageProvider(Parameters parameters) {
        this.parameters = parameters;
    }
    @Override
    public SafeStorage getSafeStorage(Storage s, String name) {
        String x = "shathel.safe." + name + ".password";
        return new SafeStorageImpl(s.getPersistedDirectory("safe/" + name),
                parameters.getParameter(x)
                        .map(pass -> pass.toCharArray())
                        .orElseThrow(() -> new RuntimeException("No password provided for " + x)));
    }
}
