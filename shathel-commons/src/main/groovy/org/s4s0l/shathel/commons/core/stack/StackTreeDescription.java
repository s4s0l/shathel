package org.s4s0l.shathel.commons.core.stack;

import com.google.common.graph.GraphBuilder;
import com.google.common.graph.Graphs;
import com.google.common.graph.ImmutableGraph;
import com.google.common.graph.MutableGraph;
import org.s4s0l.shathel.commons.utils.GraphUtils;
import org.s4s0l.shathel.commons.utils.VersionComparator;

import java.util.Optional;
import java.util.stream.Stream;

/**
 * @author Matcin Wielgus
 */
public class StackTreeDescription {


    private static class GraphNode {
        private StackDescription stack;
        private final String id;

        public GraphNode(StackDescription stack) {
            this.stack = stack;
            id = stack.getGroup() + ":" + stack.getName();
        }

    }
    public static Builder builder(StackDescription root) {
        return new Builder(new GraphNode(root));
    }

    public static class Builder {
        protected final MutableGraph<GraphNode> graph = GraphBuilder.directed().allowsSelfLoops(false).build();
        protected final GraphNode rootNode;
        protected final VersionComparator comparator = new VersionComparator();

        protected Builder(GraphNode rootNode) {
            this.rootNode = rootNode;
            graph.addNode(rootNode);
        }

        public VersionComparator getComparator() {
            return comparator;
        }



        public Optional<GraphNode> findGraphNodeById(String id) {
            return stream().filter(x -> x.id.equals(id)).findFirst();
        }


        public synchronized Builder addNode(StackReference parentId, StackDescription dep) {
            Optional<GraphNode> parent = findGraphNodeById(parentId.getGroup() + ":" + parentId.getName());
            if (!parent.isPresent()) {
                throw new RuntimeException("Parent not found");
            }
            Optional<GraphNode> graphNodeById = findGraphNodeById(dep.getGroup() + ":" + dep.getName());
            //if dep already in graph checck if it has older version
            graphNodeById
                    .filter(graphNode -> getComparator().compare(graphNode.stack.getVersion(), dep.getVersion()) < 0)
                    .ifPresent(graphNode -> graphNode.stack = dep);
            GraphNode nodeV;
            if (graphNodeById.isPresent()) {
                nodeV = graphNodeById.get();
                graph.putEdge(parent.get(), nodeV);
            } else {
                nodeV = new GraphNode(dep);
                graph.putEdge(parent.get(), nodeV);
                if(Graphs.hasCycle(graph)){
                    graph.removeNode(nodeV);
                    throw new RuntimeException("Dependency will cause cycle!!");
                }
            }

            return this;
        }

        private Stream<GraphNode> stream() {
            return GraphUtils.depthFirst(graph, rootNode);
        }

        public StackTreeDescription build(){
            return new StackTreeDescription(ImmutableGraph.copyOf(graph), rootNode);
        }
    }



    private final ImmutableGraph<GraphNode> graph;
    protected final GraphNode rootNode;

    public StackTreeDescription(ImmutableGraph<GraphNode> graph, GraphNode rootNode) {
        this.graph = graph;
        this.rootNode = rootNode;
    }

    private Stream<GraphNode> nodeStream() {
        return GraphUtils.depthFirst(graph, rootNode);
    }

    private Stream<GraphNode> nodeReverseStream() {
        return GraphUtils.depthFirstReverse(graph, rootNode);
    }

    public StackDescription getRoot(){
        return rootNode.stack;
    }

    public Stream<StackDescription> stream(){
        return nodeStream().map(x->x.stack);
    }

    public Stream<StackDescription> reverseStream(){
        return nodeReverseStream().map(x->x.stack);
    }



}
