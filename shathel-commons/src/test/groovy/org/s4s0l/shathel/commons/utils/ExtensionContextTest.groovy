package org.s4s0l.shathel.commons.utils

import org.s4s0l.shathel.commons.utils.ExtensionContext
import spock.lang.Specification

import java.util.stream.Collectors

/**
 * @author Matcin Wielgus
 */
class ExtensionContextTest extends Specification {


    interface A extends ExtensionInterface{}

    class A1 implements A {}

    class A2 extends A1 {}


    def "Test it"() {
        given:
        def a1 = new A1();
        def a2 = new A2();
        def a2x = new A2();
        def build = ExtensionContext.builder()
                .extension(a1)
                .extension(a2)
                .extension(a2x)
                .build();

        when:
        def all = build.lookupAll(A).collect(Collectors.toList())

        then:
        all == [a1,a2,a2x].reverse()

        when:
        def one = build.lookupOne(A).get()

        then:
        one == a2x

        when:
        one = build.lookupOne(A1).get()

        then:
        one == a2x

        when:
        all = build.lookupAll(A1).collect(Collectors.toList())

        then:
        all == [a1,a2,a2x].reverse()

        when:
        all = build.lookupAll(A2).collect(Collectors.toList())

        then:
        all == [a2,a2x].reverse()

    }
}
