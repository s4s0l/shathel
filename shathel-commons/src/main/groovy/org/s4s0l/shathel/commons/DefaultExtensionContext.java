package org.s4s0l.shathel.commons;

import org.s4s0l.shathel.commons.core.DefaultGlobalEnricherProvider;
import org.s4s0l.shathel.commons.core.Parameters;
import org.s4s0l.shathel.commons.core.dependencies.NoopDependencyProvider;
import org.s4s0l.shathel.commons.core.security.DefaultSafeStorageProvider;
import org.s4s0l.shathel.commons.dind.DindEnvironmentProvider;
import org.s4s0l.shathel.commons.localcompose.LocaEnvironmentProvider;
import org.s4s0l.shathel.commons.machine.vbox.VBoxMachineEnvironmentProvider;
import org.s4s0l.shathel.commons.scripts.GroovyExecutorProvider;
import org.s4s0l.shathel.commons.utils.ExtensionContext;
import org.s4s0l.shathel.commons.utils.ExtensionInterface;

import java.util.Collections;
import java.util.List;

/**
 * @author Matcin Wielgus
 */
public class DefaultExtensionContext {
    public static ExtensionContext create(Parameters parameters) {
        return getExtensionContext(parameters, Collections.emptyList());
    }

    public static ExtensionContext create(Parameters parameters, List<ExtensionInterface> extraExtensions) {
        return getExtensionContext(parameters, extraExtensions);
    }

    private static ExtensionContext getExtensionContext(Parameters parameters, List<ExtensionInterface> extraExtensions) {
        ExtensionContext.ExtensionContextBuilder extension = ExtensionContext.builder()
                .extension(new LocaEnvironmentProvider())
                .extension(new VBoxMachineEnvironmentProvider(parameters))
                .extension(new DindEnvironmentProvider(parameters))
                .extension(new DefaultSafeStorageProvider(parameters))
                .extension(new NoopDependencyProvider())
                .extension(new DefaultGlobalEnricherProvider())
                .extension(new GroovyExecutorProvider());
        for (ExtensionInterface extraExtension : extraExtensions) {
            extension.extension(extraExtension);
        }
        return extension.build();
    }


    private DefaultExtensionContext() {
    }
}
