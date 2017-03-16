package org.s4s0l.shathel.commons.core.dependencies;

import org.s4s0l.shathel.commons.utils.ExtensionContext;

import java.util.List;
import java.util.stream.Stream;

/**
 * @author Marcin Wielgus
 */
public class DependencyDownloaderRegistry {
    private final ExtensionContext extensionContext;

    public DependencyDownloaderRegistry(ExtensionContext extensionContext) {
        this.extensionContext = extensionContext;
    }


    public Stream<DependencyDownloader> getDownloaders() {
        return extensionContext.lookupAll(DependencyDownloader.class);
    }

}
