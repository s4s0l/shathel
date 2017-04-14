package org.s4s0l.shathel.commons.core;

import com.google.common.collect.Streams;
import org.s4s0l.shathel.commons.core.environment.EnricherExecutableParams;
import org.s4s0l.shathel.commons.core.environment.StackCommand;
import org.s4s0l.shathel.commons.core.environment.StackIntrospection;
import org.s4s0l.shathel.commons.core.model.ComposeFileModel;
import org.s4s0l.shathel.commons.core.stack.StackDescription;
import org.s4s0l.shathel.commons.core.stack.StackEnricherDefinition;
import org.s4s0l.shathel.commons.scripts.NamedExecutable;
import org.s4s0l.shathel.commons.scripts.ScriptExecutorProvider;
import org.s4s0l.shathel.commons.utils.ExtensionContext;
import org.s4s0l.shathel.commons.utils.VersionComparator;
import org.slf4j.Logger;

import java.util.*;
import java.util.AbstractMap.SimpleEntry;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * @author Marcin Wielgus
 */
public class StackEnricherExecutor {
    private final ExtensionContext extensionContext;
    private final Stack.StackContext stack;
    private final boolean withOptional;
    private static final Logger LOGGER = getLogger(StackEnricherExecutor.class);

    public StackEnricherExecutor(ExtensionContext extensionContext, Stack.StackContext stack, boolean withOptional) {
        this.extensionContext = extensionContext;
        this.stack = stack;
        this.withOptional = withOptional;
    }

    public StackOperations createStartSchedule() {
        Stream<StackDescription> stream = stack.getStackTreeDescription().stream();
        Stream<StackCommand> stackCommandStream = stream
                .map(stack -> new SimpleEntry<>(stack, getCommandType(stack, withOptional)))
                .filter(e -> e.getValue() != StackCommand.Type.NOOP)
                .map(e -> createStackCommand(e.getKey(), e.getValue(), withOptional));

        return buildOperations(stackCommandStream);
    }

    public StackOperations createStopSchedule(boolean withDependencies) {
        Stream<StackDescription> stream = withDependencies ?
                stack.getStackTreeDescription().reverseStream() : Stream.of(stack.getStackTreeDescription().getRoot());

        Stream<StackCommand> stackCommandStream = stream
                .map(stack -> createStackCommand(stack, StackCommand.Type.STOP, withOptional));

        return buildOperations(stackCommandStream);
    }

    private StackCommand createStackCommand(StackDescription stackDescription, StackCommand.Type commandType, boolean withOptional) {
        ComposeFileModel composeModel = stackDescription.getStackResources().getComposeFileModel();
        Map<String, String> environment = new HashMap<>();
        List<NamedExecutable> provisionersExtra;
        if (commandType.willRun) {
            provisionersExtra = Streams.concat(
                    GlobalEnricherProvider.getGlobalEnrichers(getExtensionContext()).stream()
                            .map(Optional::of),
                    getEnricherDefinitions(stackDescription).stream()
                            .map(x -> ScriptExecutorProvider.findExecutor(getExtensionContext(), x)),
                    stack.getEnvironment().getEnvironmentEnrichers().stream()
                            .map(Optional::of)
            ).flatMap(x -> execute(x, composeModel, stackDescription, environment, withOptional).stream())
                    .collect(Collectors.toList());
        } else {
            provisionersExtra = Collections.emptyList();
        }
        return new StackCommand(commandType, composeModel, stackDescription, provisionersExtra, environment);
    }

    private ExtensionContext getExtensionContext() {
        return extensionContext;
    }

    private List<NamedExecutable> execute(Optional<NamedExecutable> executor,
                                          ComposeFileModel composeModel,
                                          StackDescription stackDescription,
                                          Map<String, String> environment, boolean withOptional) {
        EnricherExecutableParams.Provisioners provisioners = new EnricherExecutableParams.Provisioners();
        EnricherExecutableParams params = new EnricherExecutableParams(
                LOGGER,
                stackDescription,
                composeModel,
                environment,
                stack, withOptional,
                provisioners
        );

        Map<String, Object> ctxt = params.toMap();
        if (executor.isPresent()) {
            LOGGER.info("Enriching with {}.", executor.get().getName());
            executor.get().execute(ctxt);
        }
        return provisioners;
    }

    private StackOperations buildOperations(Stream<StackCommand> stackCommandStream) {
        StackOperations.Builder builder = StackOperations.builder();
        stackCommandStream.forEach(x -> builder.add(x));
        return builder.build();
    }


    private List<StackEnricherDefinition> getEnricherDefinitions(StackDescription forStack) {
        return Streams.concat(
                stack.getStackTreeDescription().stream(),
                stack.getSidekicks().stream())
                .flatMap(stack ->
                        stack.getEnricherDefinitions().stream())
                .filter(def -> def.isApplicableTo(forStack))
                .collect(Collectors.toList());
    }

    private StackCommand.Type getCommandType(StackDescription stackDescription, boolean withOptional) {
        boolean self = stackDescription.getReference().equals(stack.getStackTreeDescription().getRoot().getReference());

        Optional<StackIntrospection> introspection = stack.getCurrentlyRunning(stackDescription.getReference());
        StackCommand.Type commandType = StackCommand.Type.NOOP;
        if (introspection.isPresent()) {
            if (self) {
                commandType = StackCommand.Type.UPDATE;
            } else if (new VersionComparator().compare(introspection.get().getReference().getVersion(), stackDescription.getVersion()) < 0) {
                commandType = StackCommand.Type.UPDATE;
            }
        } else {
            commandType = StackCommand.Type.START;
        }
        return commandType;
    }


}
