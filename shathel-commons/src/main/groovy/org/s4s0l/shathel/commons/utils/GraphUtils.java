package org.s4s0l.shathel.commons.utils;

import com.google.common.collect.Streams;
import com.google.common.graph.Graph;
import com.google.common.graph.Graphs;

import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Marcin Wielgus
 */
public class GraphUtils {
    public static <X> Stream<X> depthFirstReverse(Graph<X> graph, X node) {
        return StreamUtils.streamInReverse(depthFirst(graph, node).collect(Collectors.toList()));
    }

    public static <X> Stream<X> depthFirst(Graph<X> graph, X node) {
        if (Graphs.hasCycle(graph)) {
            throw new RuntimeException("Graph has cycle, unable to traverse");
        }
        return Stream.of(node).flatMap(new SetStreamFunction(graph)).distinct();
    }

    private static class SetStreamFunction<X> implements Function<X, Stream<X>> {
        private final Graph<X> graph;

        public SetStreamFunction(Graph<X> graph) {
            this.graph = graph;
        }

        @Override
        public Stream<X> apply(X xes) {
            return Streams.concat(
                    graph.successors(xes).stream().flatMap(new SetStreamFunction<X>(graph)),
                    Stream.of(xes)

            );
        }
    }
}
