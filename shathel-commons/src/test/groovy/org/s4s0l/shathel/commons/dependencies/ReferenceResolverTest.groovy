package org.s4s0l.shathel.commons.dependencies

import org.s4s0l.shathel.commons.core.MapParameters
import org.s4s0l.shathel.commons.core.dependencies.ReferenceResolver
import org.s4s0l.shathel.commons.core.dependencies.StackLocator
import org.s4s0l.shathel.commons.core.stack.StackReference
import org.s4s0l.shathel.commons.ivy.IvyDownloader
import spock.lang.Specification
import spock.lang.Unroll

/**
 * @author Marcin Wielgus
 */
class ReferenceResolverTest extends Specification {
    @Unroll
    def "for location #location should give #group #name #version"(location, group, name, version) {
        given:
        def ref = new ReferenceResolver(IvyDownloader.DEFAULT_GROUP, "1.0")
        when:
        def resolve = ref.resolve(new StackLocator(location))
        then:
        resolve.get().getGroup() == group
        resolve.get().getName() == name
        resolve.get().getVersion() == version


        where:
        location                        | group                       | name | version
        "a:b:2"                         | "a"                         | "b"  | "2"
        new StackReference("ax:bx:3.0") | "ax"                        | "bx" | "3.0"
        "b"                             | IvyDownloader.DEFAULT_GROUP | "b"  | "1.0"
        "b:2"                           | IvyDownloader.DEFAULT_GROUP | "b"  | "2"

    }
}
