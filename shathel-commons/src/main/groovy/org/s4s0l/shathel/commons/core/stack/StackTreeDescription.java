package org.s4s0l.shathel.commons.core.stack;

import com.google.common.graph.GraphBuilder;
import com.google.common.graph.Graphs;
import com.google.common.graph.ImmutableGraph;
import com.google.common.graph.MutableGraph;
import org.s4s0l.shathel.commons.utils.GraphUtils;
import org.s4s0l.shathel.commons.utils.VersionComparator;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Marcin Wielgus
 */
public class StackTreeDescription {


    public boolean contains(StackReference other) {
        return stream().anyMatch(x -> x.getReference().isSameStack(other));
    }

    private static Node ROOT_NODE = new Node() {
        @Override
        public String getId() {
            return ":";
        }

        @Override
        public StackDescription getStack() {
            throw new RuntimeException("Root has no stack, api missused");
        }

        @Override
        public void setStack(StackDescription desc) {
            throw new RuntimeException("Root has no stack, api missused");
        }

        @Override
        public boolean isRoot() {
            return true;
        }
    };
    private final ImmutableGraph<Node> graph;
    private final List<StackDescription> neighbours;


    StackTreeDescription(ImmutableGraph<Node> graph, List<StackDescription> neighbours) {
        this.graph = graph;
        this.neighbours = neighbours;
    }

    public static Builder builder() {
        return new Builder();
    }

    private static String getGraphId(StackReference parentId) {
        return parentId.getGroup() + ":" + parentId.getName();
    }

    public List<StackDescription> getNeighbours() {
        return neighbours;
    }

    private Stream<Node> nodeStream() {
        return GraphUtils.depthFirst(graph, ROOT_NODE);
    }

    private Stream<Node> nodeReverseStream() {
        return GraphUtils.depthFirstReverse(graph, ROOT_NODE);
    }

    public List<StackDescription> getRoots() {
        return graph.adjacentNodes(ROOT_NODE).stream().map(Node::getStack).collect(Collectors.toList());
    }

    public Stream<StackDescription> stream() {
        return nodeStream().filter(x -> !x.isRoot()).map(Node::getStack);
    }

    public Stream<StackDescription> reverseStream() {
        return nodeReverseStream().filter(x -> !x.isRoot()).map(Node::getStack);
    }

    private interface Node {
        String getId();

        StackDescription getStack();

        void setStack(StackDescription desc);

        boolean isRoot();
    }

    private static class GraphNode implements Node {
        private StackDescription stack;
        private final String id;

        GraphNode(StackDescription stack) {
            this.stack = stack;
            id = stack.getGroup() + ":" + stack.getName();
        }

        @Override
        public StackDescription getStack() {
            return stack;
        }

        @Override
        public void setStack(StackDescription stack) {
            this.stack = stack;
        }

        @Override
        public String getId() {
            return id;
        }

        @Override
        public boolean isRoot() {
            return false;
        }

        @Override
        public String toString() {
            return "GraphNode{" +
                    "id='" + id + '\'' +
                    '}';
        }
    }

    public static class Builder {
        final MutableGraph<Node> graph = GraphBuilder.directed().allowsSelfLoops(false).build();
        final VersionComparator comparator = new VersionComparator();
        final List<StackDescription> neighbours = new ArrayList<>();

        protected Builder() {
            graph.addNode(ROOT_NODE);
        }

        VersionComparator getComparator() {
            return comparator;
        }


        public boolean containsInGraph(StackReference other) {
            return graph.nodes().stream().map(Node::getId).anyMatch(x -> x.equals(getGraphId(other)));
        }


        Optional<Node> findGraphNodeById(String id) {
            return stream().filter(x -> x.getId().equals(id)).findFirst();
        }

        public Builder addNeighbour(StackDescription stackDescription) {
            if (!containsInGraph(stackDescription.getReference()))
                neighbours.add(stackDescription);
            return this;
        }

        public synchronized Builder addRootNode(StackDescription dep) {
            String graphId = getGraphId(dep.getReference());
            removeNeighbour(graphId);
            return addNode(ROOT_NODE.getId(), dep);
        }

        public synchronized Builder addNode(StackReference parentId, StackDescription dep) {
            String graphId = getGraphId(dep.getReference());
            removeNeighbour(graphId);
            return addNode(getGraphId(parentId), dep);
        }

        private void removeNeighbour(String graphId) {
            List<StackDescription> toRemove = neighbours.stream()
                    .filter(x -> getGraphId(x.getReference()).equals(graphId))
                    .collect(Collectors.toList());
            neighbours.removeAll(toRemove);
        }

        private synchronized Builder addNode(String parentId, StackDescription dep) {
            Optional<Node> parent = findGraphNodeById(parentId);
            if (!parent.isPresent()) {
                throw new RuntimeException("Parent not found");
            }
            Optional<Node> graphNodeById = findGraphNodeById(dep.getGroup() + ":" + dep.getName());
            //if dep already in graph checck if it has older version
            graphNodeById
                    .filter(graphNode -> getComparator().compare(graphNode.getStack().getVersion(), dep.getVersion()) < 0)
                    .ifPresent(graphNode -> graphNode.setStack(dep));
            Node nodeV;
            if (graphNodeById.isPresent()) {
                nodeV = graphNodeById.get();
                graph.putEdge(parent.get(), nodeV);
            } else {
                nodeV = new GraphNode(dep);
                graph.putEdge(parent.get(), nodeV);
                if (Graphs.hasCycle(graph)) {
                    graph.removeNode(nodeV);
                    throw new RuntimeException("Dependency will cause cycle!!");
                }
            }
            //fixing optional dependencies
            //if sth in the graph had optional dependency and optional dependencies download is off
            //and some other stack had mandatory dependency on it we include it as regular dependency
            stream()
                    .filter(it -> it.getStack().isDependantOn(dep.getReference(), true))
                    .filter(it -> !graph.successors(it).contains(nodeV))
                    .forEach(it -> graph.putEdge(it, nodeV));
            return this;
        }


        private Stream<Node> stream() {
            return GraphUtils.depthFirst(graph, ROOT_NODE);
        }

        public StackTreeDescription build() {
            return new StackTreeDescription(ImmutableGraph.copyOf(graph), neighbours);
        }
    }


}
