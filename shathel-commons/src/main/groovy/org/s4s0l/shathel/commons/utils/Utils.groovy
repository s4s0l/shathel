package org.s4s0l.shathel.commons.utils

import org.s4s0l.shathel.commons.ivy.IvyDownloader

/**
 * @author Marcin Wielgus
 */
final class Utils {
    private Utils() {
    }

    static String getShathelVersion() {
        Package pkg = IvyDownloader.class.getPackage();
        String version = null;
        if (pkg != null) {
            version = pkg.getImplementationVersion();
        }
        return (version != null ? version : "Unknown Version");
    }
}
