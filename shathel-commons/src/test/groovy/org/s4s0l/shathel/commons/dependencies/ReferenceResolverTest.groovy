package org.s4s0l.shathel.commons.dependencies

import org.s4s0l.shathel.commons.core.MapParameters
import org.s4s0l.shathel.commons.core.dependencies.ReferenceResolver
import org.s4s0l.shathel.commons.core.dependencies.StackLocator
import org.s4s0l.shathel.commons.core.stack.StackReference
import spock.lang.Specification
import spock.lang.Unroll

/**
 * @author Marcin Wielgus
 */
class ReferenceResolverTest extends Specification {
    @Unroll
    def "for location #location should give #group #name #version"(location, group, name, version) {
        given:
        def ref = new ReferenceResolver(MapParameters.builder().parameter(ReferenceResolver.SHATHEL_IVY_DEFAULT_VERSION, "1.0").build())
        when:
        def resolve = ref.resolve(new StackLocator(location))
        then:
        resolve.get().getGroup() == group
        resolve.get().getName() == name
        resolve.get().getVersion() == version


        where:
        location                        | group                           | name | version
        "a:b:2"                         | "a"                             | "b"  | "2"
        new StackReference("ax:bx:3.0") | "ax"                            | "bx" | "3.0"
        "b"                             | ReferenceResolver.DEFAULT_GROUP | "b"  | "1.0"
        "b:2"                           | ReferenceResolver.DEFAULT_GROUP | "b"  | "2"

    }
}
