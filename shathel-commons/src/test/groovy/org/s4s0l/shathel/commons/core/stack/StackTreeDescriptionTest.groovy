package org.s4s0l.shathel.commons.core.stack

import org.s4s0l.shathel.commons.core.environment.StackIntrospection
import org.s4s0l.shathel.commons.core.environment.StackIntrospectionProvider
import org.s4s0l.shathel.commons.core.model.StackFileModel
import org.yaml.snakeyaml.Yaml
import spock.lang.Specification

import java.util.stream.Collectors

/**
 * @author Marcin Wielgus
 */
class StackTreeDescriptionTest extends Specification {

    def "Should replace dependency version if newer"() {
        given:

        StackDescription root = mock("d0", "1.0", ["d1:1.0": true, "d2:1.0": false])
        StackDescription d1 = mock("d1", "1.0", ["d3:1.0": false, "d2:1.0": false])
        StackDescription d2 = mock("d2", "1.0", ["d3:1.0": true])
        StackDescription d3 = mock("d3", "1.0", [:])
        StackDescription d22 = mock("d2", "1.1", ["d3:1.0": true])
        StackDescription d33 = mock("d3", "1.0", [:])
        StackDescription s = mock("s", "1.0", ["d3:1.0": true])
        def builder = StackTreeDescription.builder(new StackIntrospectionProvider.StackIntrospections(
                [
                        new StackIntrospection(d33.reference, [], [:]),
                        new StackIntrospection(d22.reference, [], [:]),
                        new StackIntrospection(s.reference, [], [:]),
                ]
        ))

        when:
        builder.addRootNode(root, true)
        builder.addNode(d1)
        builder.addNode(d2)
        builder.addNode(d3)
        builder.addRootNode(d22, false)
        builder.addRootNode(d33, false)
        builder.addRootNode(s, false)

        def build = builder.build()

        def streamResult = build.stream().map {
            "${it.stack.name}:${it.stack.version}"
        }.collect(Collectors.toList())
        then:

        streamResult.findIndexOf { it == 's:1.0' } > streamResult.findIndexOf {
            it == 'd3:1.0'
        }
        ['d3:1.0', 'd2:1.1', 'd1:1.0', 'd0:1.0'] == streamResult - 's:1.0' //sidekick location is undeterministic

        ["s:1.0"] == build.getSidekicks(new StackReference("group:d0:1.0")).stream().map {
            "${it.stack.name}:${it.stack.version}"
        }.collect(Collectors.toList())

        //d3 is optional but as it is deployed is included in response
        ["d3:1.0", "d2:1.1", "d0:1.0"] == build.userNodesStream(false).map {
            "${it.stack.name}:${it.stack.version}"
        }.collect(Collectors.toList())


        ["d3:1.0", "d2:1.1", "d1:1.0", "d0:1.0"] == build.userNodesStream(true).map {
            "${it.stack.name}:${it.stack.version}"
        }.collect(Collectors.toList())

        ["d2:1.1", "d1:1.0", "d0:1.0"].reverse() == build.userNodesIsolatedDepsReverseStream(true).map {
            "${it.stack.name}:${it.stack.version}"
        }.collect(Collectors.toList())

        ["d0:1.0"].reverse() == build.userNodesIsolatedDepsReverseStream(false).map {
            "${it.stack.name}:${it.stack.version}"
        }.collect(Collectors.toList())
    }

    private StackDescription mock(String name, String version, Map<String, Boolean> deps) {
        new StackDescriptionImpl(new StackFileModel(new Yaml().load("""
version: 1
shathel-stack:
  gav: group:${name}:${version}  
  dependencies:
${
            deps.collect {
                "   group:${it.key}:${it.value ? "\n     optional: true" : ""}"
            }.join("\n")
        }
""")), null, null)
    }


}
