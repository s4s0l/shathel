package org.s4s0l.shathel.commons;

import org.s4s0l.shathel.commons.bin.BinaryManagerExtension;
import org.s4s0l.shathel.commons.bin.BinaryManagerImpl;
import org.s4s0l.shathel.commons.bin.DefaultBinaryLocators;
import org.s4s0l.shathel.commons.core.DefaultGlobalEnricherProvider;
import org.s4s0l.shathel.commons.core.Parameters;
import org.s4s0l.shathel.commons.core.dependencies.FileStackDependencyDownloader;
import org.s4s0l.shathel.commons.core.security.DefaultSafeStorageProvider;
import org.s4s0l.shathel.commons.git.GitStackDependencyDownloader;
import org.s4s0l.shathel.commons.ivy.IvyDownloader;
import org.s4s0l.shathel.commons.localswarm.LocalSwarmEnvironmentProvider;
import org.s4s0l.shathel.commons.remoteswarm.downloader.FileEnvironmentPackageDownloader;
import org.s4s0l.shathel.commons.remoteswarm.downloader.GitEnvironmentPackageDownloader;
import org.s4s0l.shathel.commons.remoteswarm.provider.RemoteEnvironmentProvider;
import org.s4s0l.shathel.commons.scripts.ansible.AnsibleExecutorProvider;
import org.s4s0l.shathel.commons.scripts.groovy.GroovyExecutorProvider;
import org.s4s0l.shathel.commons.scripts.packer.PackerExecutorProvider;
import org.s4s0l.shathel.commons.scripts.terraform.TerraformExecutorProvider;
import org.s4s0l.shathel.commons.scripts.vaagrant.VagrantExecutorProvider;
import org.s4s0l.shathel.commons.utils.ExtensionContext;
import org.s4s0l.shathel.commons.utils.ExtensionInterface;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author Marcin Wielgus
 */
public class DefaultExtensionContext {
    public static ExtensionContextsProvider create() {
        return getExtensionContext(Collections.emptyList());
    }

    public static ExtensionContextsProvider createWithFunctions(List<Function<Parameters, ExtensionInterface>> extraExtensions) {
        return getExtensionContext(extraExtensions);
    }

    public static ExtensionContextsProvider create(List<ExtensionInterface> extraExtensions) {
        return getExtensionContext(toFunctions(extraExtensions));
    }

    private static ExtensionContextsProvider getExtensionContext(List<Function<Parameters, ExtensionInterface>> extraExtensions) {
        ExtensionContextsProvider.ExtensionContextsProviderBuilder extension = ExtensionContextsProvider.builder()
                .extension(parameterProvider -> new LocalSwarmEnvironmentProvider())
                .extension(parameterProvider -> new RemoteEnvironmentProvider())
                .extension(parameterProvider -> new BinaryManagerExtension(parameterProvider))
                .extension(parameterProvider -> new GitEnvironmentPackageDownloader(parameterProvider))
                .extension(parameterProvider -> new FileEnvironmentPackageDownloader(parameterProvider))
                .extensions(DefaultBinaryLocators.getDefaultLocators().stream()
                        .map(DefaultExtensionContext::toFunction).collect(Collectors.toList()))
                .extension(parameterProvider -> new DefaultSafeStorageProvider(parameterProvider))
                .extension(parameterProvider -> new DefaultGlobalEnricherProvider())
                .extension(parameterProvider -> new GroovyExecutorProvider())
                .extension(parameterProvider -> new AnsibleExecutorProvider())
                .extension(parameterProvider -> new VagrantExecutorProvider())
                .extension(parameterProvider -> new TerraformExecutorProvider())
                .extension(parameterProvider -> new PackerExecutorProvider())
                .extension(parameterProvider -> new IvyDownloader(parameterProvider))
                .extension(parameterProvider -> new GitStackDependencyDownloader(parameterProvider))
                .extension(parameterProvider -> new FileStackDependencyDownloader(parameterProvider));

        for (Function<Parameters, ExtensionInterface> extraExtension : extraExtensions) {
            extension.extension(extraExtension);
        }
        return extension.build();
    }

    static Function<Parameters, ExtensionInterface> toFunction(ExtensionInterface iface) {
        return p -> iface;
    }

    static List<Function<Parameters, ExtensionInterface>> toFunctions(List<ExtensionInterface> iface) {
        return iface.stream().map(it -> toFunction(it)).collect(Collectors.toList());
    }


    private DefaultExtensionContext() {
    }
}
