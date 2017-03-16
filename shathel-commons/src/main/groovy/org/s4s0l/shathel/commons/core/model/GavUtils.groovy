package org.s4s0l.shathel.commons.core.model

/**
 * @author Marcin Wielgus
 */
class GavUtils {



    static String getName(String gav) {
        def split = gav.split(":")
        assert split.length == 3
        return split[1]
    }

    static String getGroup(String gav) {
        def split = gav.split(":")
        assert split.length == 3
        return split[0]
    }

    static String getVersion(String gav) {
        def split = gav.split(":")
        assert split.length == 3
        return split[2]
    }
}
