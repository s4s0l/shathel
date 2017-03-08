package org.s4s0l.shathel.commons;

import org.s4s0l.shathel.commons.core.DefaultGlobalEnricherProvider;
import org.s4s0l.shathel.commons.core.Parameters;
import org.s4s0l.shathel.commons.core.dependencies.NoopDependencyProvider;
import org.s4s0l.shathel.commons.core.security.DefaultSafeStorageProvider;
import org.s4s0l.shathel.commons.dind.DindEnvironmentProvider;
import org.s4s0l.shathel.commons.localcompose.LocalEnvironmentProvider;
import org.s4s0l.shathel.commons.localswarm.LocalSwarmEnvironmentProvider;
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
    public static ExtensionContext create(Parameters parameterProvider) {
        return getExtensionContext(parameterProvider, Collections.emptyList());
    }

    public static ExtensionContext create(Parameters parameterProvider, List<ExtensionInterface> extraExtensions) {
        return getExtensionContext(parameterProvider, extraExtensions);
    }

    private static ExtensionContext getExtensionContext(Parameters parameterProvider, List<ExtensionInterface> extraExtensions) {
        ExtensionContext.ExtensionContextBuilder extension = ExtensionContext.builder()
                .extension(new LocalEnvironmentProvider())
                .extension(new LocalSwarmEnvironmentProvider())
                .extension(new VBoxMachineEnvironmentProvider(parameterProvider))
                .extension(new DindEnvironmentProvider())
                .extension(new DefaultSafeStorageProvider(parameterProvider))
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
