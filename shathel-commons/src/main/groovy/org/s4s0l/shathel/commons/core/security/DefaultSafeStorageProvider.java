package org.s4s0l.shathel.commons.core.security;

import org.s4s0l.shathel.commons.core.ParameterProvider;

import java.io.File;

/**
 * @author Matcin Wielgus
 */
public class DefaultSafeStorageProvider implements SafeStorageProvider {
    private final ParameterProvider parameterProvider;

    public DefaultSafeStorageProvider(ParameterProvider parameterProvider) {
        this.parameterProvider = parameterProvider;
    }

    @Override
    public SafeStorage getSafeStorage(File s, String name) {
        String x = "shathel.env." + name + ".safePassword";
        return new SafeStorageImpl(s,
                parameterProvider.getParameter(x)
                        .map(pass -> pass.toCharArray())
                        .orElseThrow(() -> new RuntimeException("No password provided for " + x)));
    }
}
