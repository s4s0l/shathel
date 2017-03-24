package org.s4s0l.shathel.commons.core.dependencies

import org.s4s0l.shathel.commons.core.ParameterProvider
import org.s4s0l.shathel.commons.core.dependencies.StackLocator
import org.s4s0l.shathel.commons.core.stack.StackReference
import org.s4s0l.shathel.commons.ivy.IvyDownloader

/**
 * @author Marcin Wielgus
 */
class ReferenceResolver {


    private final String defaultGroup;
    private final String defaultVersion;

    ReferenceResolver(String defaultGroup, String defaultVersion) {
        this.defaultGroup = defaultGroup
        this.defaultVersion = defaultVersion
    }



    String getDefaultVersion() {
        return defaultVersion
    }

    String getDefaultGroup() {
        return defaultGroup
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
