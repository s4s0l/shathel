package org.s4s0l.shathel.commons;

import org.s4s0l.shathel.commons.bin.BinaryManagerExtension;
import org.s4s0l.shathel.commons.bin.BinaryManagerImpl;
import org.s4s0l.shathel.commons.bin.DefaultBinaryLocators;
import org.s4s0l.shathel.commons.core.DefaultGlobalEnricherProvider;
import org.s4s0l.shathel.commons.core.Parameters;
import org.s4s0l.shathel.commons.core.dependencies.FileStackDependencyDownloader;
import org.s4s0l.shathel.commons.core.security.DefaultSafeStorageProvider;
import org.s4s0l.shathel.commons.dind.DindEnvironmentProvider;
import org.s4s0l.shathel.commons.git.GitStackDependencyDownloader;
import org.s4s0l.shathel.commons.ivy.IvyDownloader;
import org.s4s0l.shathel.commons.localcompose.LocalEnvironmentProvider;
import org.s4s0l.shathel.commons.localswarm.LocalSwarmEnvironmentProvider;
import org.s4s0l.shathel.commons.machine.vbox.VBoxMachineEnvironmentProvider;
import org.s4s0l.shathel.commons.remoteswarm.downloader.FileEnvironmentPackageDownloader;
import org.s4s0l.shathel.commons.remoteswarm.downloader.GitEnvironmentPackageDownloader;
import org.s4s0l.shathel.commons.remoteswarm.provider.RemoteEnvironmentProvider;
import org.s4s0l.shathel.commons.scripts.groovy.GroovyExecutorProvider;
import org.s4s0l.shathel.commons.utils.ExtensionContext;
import org.s4s0l.shathel.commons.utils.ExtensionInterface;

import java.util.Collections;
import java.util.List;

/**
 * @author Marcin Wielgus
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
                .extension(new RemoteEnvironmentProvider())
                .extension(new BinaryManagerExtension(parameterProvider))
                .extension(new DindEnvironmentProvider())
                .extension(new FileEnvironmentPackageDownloader(parameterProvider))
                .extension(new GitEnvironmentPackageDownloader(parameterProvider))
                .extensions(DefaultBinaryLocators.getDefaultLocators())
                .extension(new DefaultSafeStorageProvider(parameterProvider))
                .extension(new DefaultGlobalEnricherProvider())
                .extension(new GroovyExecutorProvider())
                .extension(new IvyDownloader(parameterProvider))
                .extension(new GitStackDependencyDownloader(parameterProvider))
                .extension(new FileStackDependencyDownloader(parameterProvider));

        for (ExtensionInterface extraExtension : extraExtensions) {
            extension.extension(extraExtension);
        }
        return extension.build();
    }


    private DefaultExtensionContext() {
    }
}
