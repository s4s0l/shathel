package org.s4s0l.shathel.commons.core;

import com.google.common.collect.Streams;
import org.apache.commons.collections.map.HashedMap;
import org.s4s0l.shathel.commons.core.environment.Environment;
import org.s4s0l.shathel.commons.core.environment.StackIntrospection;
import org.s4s0l.shathel.commons.core.environment.StackIntrospectionProvider;
import org.s4s0l.shathel.commons.core.model.ComposeFileModel;
import org.s4s0l.shathel.commons.core.provision.StackCommand;
import org.s4s0l.shathel.commons.core.stack.StackDescription;
import org.s4s0l.shathel.commons.core.stack.StackEnricherDefinition;
import org.s4s0l.shathel.commons.core.stack.StackProvisionerDefinition;
import org.s4s0l.shathel.commons.core.stack.StackTreeDescription;
import org.s4s0l.shathel.commons.scripts.Executor;
import org.s4s0l.shathel.commons.scripts.ScriptExecutorProvider;
import org.s4s0l.shathel.commons.utils.ExtensionContext;
import org.s4s0l.shathel.commons.utils.VersionComparator;

import java.util.AbstractMap.SimpleEntry;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Matcin Wielgus
 */
public class StackOperationsFactory {
    private final StackTreeDescription descriptionTree;
    private final StackIntrospectionProvider introspectionProvider;
    private final Environment environment;

    public StackOperationsFactory(StackTreeDescription descriptionTree,
                                  Environment environment) {
        this.descriptionTree = descriptionTree;
        this.environment = environment;
        this.introspectionProvider = environment.getIntrospectionProvider();
    }

    public StackOperations createStartSchedule() {

        Stream<StackDescription> stream = descriptionTree.stream();

        Stream<StackCommand> stackCommandStream = stream
                .map(stack -> new SimpleEntry<>(stack, getCommandType(stack)))
                .filter(e -> e.getValue() != StackCommand.Type.NOOP)
                .map(e -> createStackCommand(e.getKey(), e.getValue()));

        return buildOperations(stackCommandStream);
    }

    public StackOperations createStopSchedule(boolean withDependencies) {
        Stream<StackDescription> stream = withDependencies ?
                descriptionTree.reverseStream() : Stream.of(descriptionTree.getRoot());

        Stream<StackCommand> stackCommandStream = stream
                .map(stack -> createStackCommand(stack, StackCommand.Type.STOP));

        return buildOperations(stackCommandStream);
    }

    private StackCommand createStackCommand(StackDescription stack, StackCommand.Type commandType) {
        ComposeFileModel composeModel = stack.getStackResources().getComposeFileModel();
        List<Executor> provisionersExtra = commandType.willRun ? Streams.concat(
                getEnricherDefinitions(stack).stream()
                        .map(x -> ScriptExecutorProvider.findExecutor(getExtensionContext(), x)),
                GlobalEnricherProvider.getGlobalEnrichers(getExtensionContext()).stream()
                        .map(Optional::of),
                environment.getEnvironmentEnrichers().stream()
                        .map(Optional::of)
        ).flatMap(x -> execute(x, composeModel, stack).stream())
                .collect(Collectors.toList()) : Collections.emptyList();
        return new StackCommand(commandType, composeModel, stack, provisionersExtra);
    }

    private ExtensionContext getExtensionContext() {
        return environment.getEnvironmentContext().getExtensionContext();
    }

    private List<Executor> execute(Optional<Executor> executor,
                                                     ComposeFileModel composeModel,
                                                     StackDescription stack) {
        Map<String, Object> ctxt = new HashedMap();
        ctxt.put("context", environment.getEnvironmentContext());
        ctxt.put("env", environment.getEnvironmentApiFacade());
        ctxt.put("compose", composeModel);
        ctxt.put("stack", stack);

        return executor
                .map(x -> x.execute(ctxt))
                .map(o -> (List<Executor>) o)
                .orElse(Collections.emptyList());
    }

    private StackOperations buildOperations(Stream<StackCommand> stackCommandStream) {
        StackOperations.Builder builder = StackOperations.builder();
        stackCommandStream.forEach(x -> builder.add(x));
        return builder.build();
    }


    private List<StackEnricherDefinition> getEnricherDefinitions(StackDescription forStack) {
        return descriptionTree.stream().flatMap(stack ->
                stack.getEnricherDefinitions().stream().filter(def -> def.isApplicableTo(forStack))
        ).collect(Collectors.toList());
    }

    private StackCommand.Type getCommandType(StackDescription stack) {
        boolean self = stack.getReference().equals(descriptionTree.getRoot().getReference());

        Optional<StackIntrospection> introspection = introspectionProvider.getIntrospection(stack.getReference());
        StackCommand.Type commandType = StackCommand.Type.NOOP;
        if (introspection.isPresent()) {
            if (self ) {
                commandType = StackCommand.Type.UPDATE;
            } else if (new VersionComparator().compare(introspection.get().getReference().getVersion(), stack.getVersion()) < 0) {
                commandType = StackCommand.Type.UPDATE;
            }
        } else {
            commandType = StackCommand.Type.START;
        }
        return commandType;
    }


}
