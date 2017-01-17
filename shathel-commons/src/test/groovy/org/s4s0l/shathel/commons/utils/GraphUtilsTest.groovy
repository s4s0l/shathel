package org.s4s0l.shathel.commons.utils

import com.google.common.graph.GraphBuilder
import com.google.common.graph.MutableGraph
import spock.lang.Specification

import java.util.stream.Collectors

/**
 * @author Matcin Wielgus
 */
class GraphUtilsTest extends Specification {

    def "Guava samples"() {
        given:
        MutableGraph<Integer> graph = GraphBuilder.directed().allowsSelfLoops(false).build();
        when:
        graph.addNode(1)
        graph.putEdge(1, 2)
        graph.putEdge(1, 3)
        graph.putEdge(3, 2)
        graph.putEdge(2, 4)
        def list = GraphUtils.depthFirst(graph, 1).collect(Collectors.toList())
        then:
        list == [4, 2, 3, 1]
    }

}
