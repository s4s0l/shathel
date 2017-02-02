package org.s4s0l.shathel.commons.core.model

/**
 * @author Matcin Wielgus
 */
class GavUtils {


    public static final String DEFAULT_GROUP = 'org.s4s0l.shathel'

    static String getName(String gav) {
        def split = gav.split(":")
        return split.length == 2 ? split[0] : split[1]
    }

    static String getGroup(String gav) {
        def split = gav.split(":")
        return split.length == 2 ? DEFAULT_GROUP : split[0]
    }

    static String getVersion(String gav) {
        def split = gav.split(":")
        return split.length == 2 ? split[1] : split[2]
    }
}
