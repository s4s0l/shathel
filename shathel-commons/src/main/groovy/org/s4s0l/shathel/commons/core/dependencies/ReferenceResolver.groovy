package org.s4s0l.shathel.commons.core.dependencies

import org.s4s0l.shathel.commons.core.ParameterProvider
import org.s4s0l.shathel.commons.core.dependencies.StackLocator
import org.s4s0l.shathel.commons.core.stack.StackReference
import org.s4s0l.shathel.commons.ivy.IvyDownloader

/**
 * @author Marcin Wielgus
 */
class ReferenceResolver {
    public static
    final String SHATHEL_IVY_DEFAULT_VERSION = "shathel.ivy.default_version"
    public static
    final String SHATHEL_IVY_DEFAULT_GROUP = "shathel.ivy.default_group"
    public static final String DEFAULT_GROUP = "org.s4s0l.shathel"
    private final ParameterProvider params;

    ReferenceResolver(ParameterProvider params) {
        this.params = params
    }


    static String getShathelVersion() {
        Package pkg = IvyDownloader.class.getPackage();
        String version = null;
        if (pkg != null) {
            version = pkg.getImplementationVersion();
        }
        return (version != null ? version : "Unknown Version");
    }

    String getDefaultVersion() {
        params.getParameter(SHATHEL_IVY_DEFAULT_VERSION).orElse(shathelVersion)
    }

    String getDefaultGroup() {
        params.getParameter(SHATHEL_IVY_DEFAULT_GROUP).orElse(DEFAULT_GROUP)
    }

    Optional<StackReference> resolve(StackLocator locator) {
        if (locator.reference.isPresent()) {
            return locator.reference;
        } else {
            def location = locator.location
            def split = location.split(":")
            if (split.length == 3) {
                return Optional.of(new StackReference(location))
            }
            if (split.length == 2) {
                return Optional.of(new StackReference(getDefaultGroup(), split[0], split[1]))
            }
            if (split.length == 1) {
                return Optional.of(new StackReference(getDefaultGroup(), split[0], getDefaultVersion()))
            }
            return Optional.empty()
        }
    }

}
