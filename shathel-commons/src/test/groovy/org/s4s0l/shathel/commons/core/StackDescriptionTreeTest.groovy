package org.s4s0l.shathel.commons.core

import com.google.common.graph.GraphBuilder
import com.google.common.graph.MutableGraph
import org.mockito.Mockito
import spock.lang.Specification

import java.util.stream.Collector
import java.util.stream.Collectors

/**
 * @author Matcin Wielgus
 */
class StackDescriptionTreeTest extends Specification {

    def "Should replace dependency version if newer"() {
        given:

        StackDescription root = mock("a", "1.0");
        StackDescription d1 = mock("d1", "1.0");
        StackDescription d2 = mock("d2", "1.0");
        StackDescription d3 = mock("d3", "1.0");
        StackDescription d22 = mock("d2", "1.1");
        def builder = StackDescriptionTree.builder(root)

        when:
        builder.addNode(root.reference, d1)
        builder.addNode(root.reference, d2)
        builder.addNode(d1.reference, d3)
        builder.addNode(d3.reference, d22)

        def build = builder.build();
        then:

        ["d2:1.1","d3:1.0","d1:1.0","a:1.0"] == build.stream().map {
            "${it.name}:${it.version}"
        }.collect(Collectors.toList())


    }

    private StackDescription mock(String name, String version) {
        def mock = Mockito.mock(StackDescription)
        Mockito.when(mock.name).thenReturn(name)
        Mockito.when(mock.version).thenReturn(version)
        Mockito.when(mock.group).thenReturn("group")
        Mockito.when(mock.reference).thenReturn(new StackReference(name,"group", version))
        mock
    }


}
