package org.s4s0l.shathel.commons;

import org.s4s0l.shathel.commons.core.DefaultGlobalEnricherProvider;
import org.s4s0l.shathel.commons.core.Parameters;
import org.s4s0l.shathel.commons.core.dependencies.NoopDependencyProvider;
import org.s4s0l.shathel.commons.core.enricher.DefaultEnricherProvider;
import org.s4s0l.shathel.commons.core.security.DefaultSafeStorageProvider;
import org.s4s0l.shathel.commons.localcompose.LocaEnvironmentProvider;
import org.s4s0l.shathel.commons.machine.vbox.VBoxMachineEnvironmentProvider;
import org.s4s0l.shathel.commons.utils.ExtensionContext;

/**
 * @author Matcin Wielgus
 */
public class DefaultExtensionContext {
    public static ExtensionContext create(Parameters parameters) {
        return ExtensionContext.builder()
                .extension(new LocaEnvironmentProvider())
                .extension(new VBoxMachineEnvironmentProvider(parameters))
                .extension(new DefaultSafeStorageProvider(parameters))
                .extension(new NoopDependencyProvider())
                .extension(new DefaultEnricherProvider())
                .extension(new DefaultGlobalEnricherProvider())
                .build();
    }


    private DefaultExtensionContext() {
    }
}
