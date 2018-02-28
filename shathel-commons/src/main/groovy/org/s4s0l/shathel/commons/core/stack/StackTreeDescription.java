package org.s4s0l.shathel.commons.core.stack;

import com.google.common.graph.GraphBuilder;
import com.google.common.graph.Graphs;
import com.google.common.graph.ImmutableGraph;
import com.google.common.graph.MutableGraph;
import org.s4s0l.shathel.commons.core.environment.StackIntrospection;
import org.s4s0l.shathel.commons.core.environment.StackIntrospectionProvider;
import org.s4s0l.shathel.commons.utils.GraphUtils;
import org.s4s0l.shathel.commons.utils.VersionComparator;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Marcin Wielgus
 */
public class StackTreeDescription {
    private static Node ROOT_NODE = new RootNode(":");
    private final ImmutableGraph<Node> graph;
    private final StackIntrospectionProvider.StackIntrospections stackIntrospections;

    StackTreeDescription(ImmutableGraph<Node> graph, StackIntrospectionProvider.StackIntrospections stackIntrospections) {
        this.graph = graph;
        this.stackIntrospections = stackIntrospections;
    }

    public static Builder builder(StackIntrospectionProvider.StackIntrospections stackIntrospections) {
        return new Builder(stackIntrospections);
    }

    private static String getGraphId(StackReference parentId) {
        return parentId.getGroup() + ":" + parentId.getName();
    }

    public boolean contains(StackReference other) {
        return stream().anyMatch(x -> x.getStack().getReference().isSameStack(other));
    }

    private Optional<Node> findGraphNodeById(String graphId) {
        return nodeStream(ROOT_NODE).filter(x -> getGraphId(x.getStack().getReference()).equals(graphId)).findFirst();
    }

    /**
     * Return list of all stacks that given reference has no relation to
     */
    public List<StackTreeNode> getSidekicks(StackReference reference) {
        return findGraphNodeById(getGraphId(reference))
                .map(x -> {
                    List<Node> allDependant = GraphUtils.depthFirst(graph, x).collect(Collectors.toList());
                    return nodeStream(ROOT_NODE)
                            .filter(n -> !allDependant.contains(n))
                            .map(m -> (StackTreeNode) m)
                            .collect(Collectors.toList());
                })
                .orElseGet(() -> {
                    throw new RuntimeException("Reference " + reference + " not found in graph");
                });
    }

    private Stream<StackTreeNode> rootUserNodes() {
        return graph.successors(ROOT_NODE).stream()
                .filter(StackTreeNode::isUserRequest)
                .map(it -> (StackTreeNode) it);
    }

    private Stream<Node> nodeStream(Node from) {
        return GraphUtils.depthFirst(graph, from).filter(x -> !x.isRoot());
    }

    private Stream<Node> nodeReverseStream(Node from) {
        return GraphUtils.depthFirstReverse(graph, from).filter(x -> !x.isRoot());
    }

    public Stream<StackTreeNode> stream() {
        return nodeStream(ROOT_NODE).filter(x -> !x.isRoot()).map(x -> (StackTreeNode) x);
    }

    private Stream<StackTreeNode> reverseStream() {
        return nodeReverseStream(ROOT_NODE).filter(x -> !x.isRoot()).map(x -> (StackTreeNode) x);
    }


    /**
     * contains dependencies reversed stream containing all dependencies of from
     * without ones that have some stack not in from but in graph that is
     * dependant on. In other words it we want to stop froms we would get here
     * list of all dependencies of these in from that are also safe to stop
     */
    public Stream<StackTreeNode> userNodesIsolatedDepsReverseStream(boolean includeOptional) {
        Set<StackReference> from = rootUserNodes().map(it -> it.getStack().getReference()).collect(Collectors.toSet());
        StackTreeDescription toRemove = getSubTree(
                rootUserNodes()
                , it -> includeOptional || !it.isOptional());

        Set<StackReference> toRemoveAllRefs = toRemove.stream().map(it -> it.getStack().getReference()).collect(Collectors.toSet());

        Set<StackReference> doNotTouch = getSubTree(
                stream().filter(it -> !toRemoveAllRefs.contains(it.getStack().getReference()))
                , it -> true).stream().map(it -> it.getStack().getReference()).collect(Collectors.toSet());

        return toRemove.reverseStream()
                .filter(it -> from.contains(it.getStack().getReference()) || !doNotTouch.contains(it.getStack().getReference()));
    }

    public Stream<StackTreeNode> userNodesStream(boolean includeOptional) {
        return getSubTree(
                rootUserNodes(),
                it -> includeOptional
                        || !it.isOptional()
                        || findGraphNodeById(getGraphId(it.getStackReference()))
                        .map(x -> x.getIntrospection().isPresent())
                        .orElse(false)
        ).stream();
    }


    /**
     * Rebuilds graph using only root nodes excluding all optional dependencies
     */
    private StackTreeDescription getSubTree(Stream<StackTreeNode> startingNodes, Predicate<StackDependency> depSelector) {
        Builder builder = builder(stackIntrospections);
        startingNodes.forEach(it -> {
            builder.addRootNode(it.getStack(), it.isUserRequest());
            addUserNonOptionalRecursion(builder, it.getStack(), depSelector);
        });
        return builder.build();
    }

    private void addUserNonOptionalRecursion(Builder builder, StackDescription node, Predicate<StackDependency> depSelector) {
        node.getDependencies().stream()
                .filter(depSelector)
                .map(it -> findGraphNodeById(getGraphId(it.getStackReference())))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .forEach(it -> {
                    builder.addNode(it.getStack());
                    addUserNonOptionalRecursion(builder, it.getStack(), depSelector);
                });
    }


    public interface StackTreeNode {
        StackDescription getStack();

        boolean isUserRequest();

        Optional<StackIntrospection> getIntrospection();

    }

    private interface Node extends StackTreeNode {
        String getId();

        void setStack(StackDescription desc);

        void setUserRequest(boolean userRequest);

        boolean isRoot();
    }

    private static class RootNode implements Node {
        private final String id;

        private RootNode(String id) {
            this.id = id;
        }

        @Override
        public boolean isUserRequest() {
            return false;
        }

        @Override
        public void setUserRequest(boolean userRequest) {

        }

        @Override
        public Optional<StackIntrospection> getIntrospection() {
            return Optional.empty();
        }


        @Override
        public String getId() {
            return id;
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
    }

    private static class GraphNode implements Node {
        private StackDescription stack;
        private final String id;
        private StackIntrospection introspection;
        private boolean userRequest;

        GraphNode(StackDescription stack, boolean userRequest) {
            this.stack = stack;
            this.id = stack.getGroup() + ":" + stack.getName();
            this.introspection = null;
            this.userRequest = userRequest;
        }


        GraphNode(StackDescription stack, boolean userRequest, StackIntrospection introspection) {
            this.stack = stack;
            this.id = stack.getGroup() + ":" + stack.getName();
            this.introspection = introspection;
            this.userRequest = userRequest;
        }


        @Override
        public boolean isUserRequest() {
            return userRequest;
        }

        @Override
        public void setUserRequest(boolean userRequest) {
            this.userRequest = userRequest;
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
        public Optional<StackIntrospection> getIntrospection() {
            return Optional.ofNullable(introspection);
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
        final StackIntrospectionProvider.StackIntrospections stackIntrospections;
        final VersionComparator comparator = new VersionComparator();

        protected Builder(StackIntrospectionProvider.StackIntrospections stackIntrospections) {
            this.stackIntrospections = stackIntrospections;
            graph.addNode(ROOT_NODE);

        }

        VersionComparator getComparator() {
            return comparator;
        }

        private Optional<Node> findGraphNodeById(StackReference id) {
            return stream().filter(x -> x.getId().equals(getGraphId(id))).findFirst();
        }


        public synchronized Builder addRootNode(StackDescription dep, boolean userRequest) {
            return addNode(true, dep, userRequest);
        }

        public synchronized Builder addNode(StackDescription dep) {
            return addNode(false, dep, false);
        }


        private synchronized Builder addNode(boolean root, StackDescription depi, boolean userRequest) {
            Optional<Node> graphNodeById = findGraphNodeById(depi.getReference());
            Node nodeV;
            if (graphNodeById.isPresent()) {
                nodeV = graphNodeById.get();
                //if dep already in graph check if it has older version
                if (getComparator().compare(nodeV.getStack().getVersion(), depi.getVersion()) < 0) {
                    nodeV.setStack(depi);
                }
                //make sure this dependency has flag user request
                if (userRequest) {
                    nodeV.setUserRequest(true);
                }
            } else {
                Optional<StackIntrospection> introspection = stackIntrospections.getIntrospection(depi.getReference());
                nodeV = introspection
                        .map(stackIntrospection -> new GraphNode(depi, userRequest, stackIntrospection))
                        .orElseGet(() -> new GraphNode(depi, userRequest));
                graph.addNode(nodeV);
            }
            if (root) {
                graph.putEdge(ROOT_NODE, nodeV);
            }
            if (Graphs.hasCycle(graph)) {
                graph.removeNode(nodeV);
                throw new RuntimeException("Dependency will cause cycle!!");
            }

            //we remove nodes in case older versions had different dependencies
            Set<Node> successors = new HashSet<>(graph.successors(nodeV));
            successors.forEach(it -> graph.removeEdge(nodeV, it));

            //we look for all stacks that can be dependant upon this and we add them
            stream()
                    .filter(it -> !it.isRoot())
                    .filter(it -> it.getStack().isDependantOn(nodeV.getStack().getReference(), true))
                    .filter(it -> !graph.successors(it).contains(nodeV))
                    .forEach(it -> graph.putEdge(it, nodeV));

            //we add all dependencies of this one if dependants already in graph
            nodeV.getStack().getDependencies().stream()
                    .map(it -> findGraphNodeById(it.getStackReference()))
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .filter(it -> !graph.successors(nodeV).contains(it))
                    .forEach(it -> graph.putEdge(nodeV, it));


            return this;
        }


        private Stream<Node> stream() {
            return GraphUtils.depthFirst(graph, ROOT_NODE);
        }


        public StackTreeDescription build() {
            return new StackTreeDescription(ImmutableGraph.copyOf(graph), stackIntrospections);
        }
    }


}
