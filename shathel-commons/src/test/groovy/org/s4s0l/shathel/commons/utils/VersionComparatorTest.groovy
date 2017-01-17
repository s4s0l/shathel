package org.s4s0l.shathel.commons.utils

import spock.lang.Specification
import spock.lang.Unroll

/**
 * @author Matcin Wielgus
 */
class VersionComparatorTest extends Specification {

    @Unroll
    def "checks if version #version1 vs #version2 is #expected"(version1, version2, expected) {
        given:
        VersionComparator comparator = new VersionComparator()

        when:
        def compare = comparator.compare(version1, version2)

        then:
        compare == expected

        where:
        version1   | version2  | expected
        "1"        | "1"       | 0
        "1"        | "2"       | -1
        "3"        | "2"       | 1
        "3.0"      | "3"       | 0
        "3.1"      | "3.2"     | -1
        "3.1.rc1"  | "3.1.rc0" | 1
        "3.1"      | "3.1.2"   | -1
        "3.10"     | "3.1"     | 1
// tak byłoby fajnie        "3.10-rc1" | "3.10"    | -1


    }
}
