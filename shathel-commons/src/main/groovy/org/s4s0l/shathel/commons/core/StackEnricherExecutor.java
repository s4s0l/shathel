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

import java.io.File;
import java.util.AbstractMap.SimpleEntry;
import java.util.*;
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

    StackEnricherExecutor(ExtensionContext extensionContext, Stack.StackContext stack, boolean withOptional) {
        this.extensionContext = extensionContext;
        this.stack = stack;
        this.withOptional = withOptional;
    }

    public StackOperations createStartSchedule() {
        Stream<StackDescription> stream = stack.getStackTreeDescription().stream();
        Stream<StackCommand> stackCommandStream = stream
                .map(stack -> new SimpleEntry<>(stack, getCommandType(stack)))
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
        //fixme: Ogólnie całe to szukanie tego katalogu 'parent' jest słabe tu, typedScript powinien mieć psi obowiązek dostarczenia go
        if (commandType.willRun) {
            Stream<NamedExecutable> fromGlobal = GlobalEnricherProvider.getGlobalEnrichers(getExtensionContext())
                    .stream()
                    .flatMap(x -> {
                        File parentScriptsLocation = stackDescription.getStackResources().getStackDirectory();
                        String parentName = x.getName();
                        return execute(x, composeModel, stackDescription,
                                parentScriptsLocation, parentName,
                                environment, withOptional).stream();
                    });
            Stream<NamedExecutable> fromStacks = getEnricherDefinitions(stackDescription).stream()
                    .flatMap(x -> {
                        Optional<NamedExecutable> executor = ScriptExecutorProvider.findExecutor(getExtensionContext(), x);
                        if (executor.isPresent()) {
                            File parentDir = executor.get().getScript().getScriptFileLocation()
                                    .map(File::getParentFile)
                                    .orElseGet(() -> x.getOrigin().getStackResources().getStackDirectory());
                            String parentName = executor.get().getName();
                            return execute(executor.get(), composeModel, stackDescription,
                                    parentDir, parentName,
                                    environment, withOptional).stream();
                        } else {
                            return Stream.empty();
                        }
                    });
            Stream<NamedExecutable> fromEnv = stack.getEnvironment().getEnvironmentEnrichers().stream()
                    .flatMap(x -> {
                        //fixme: na logikę tu powinien być katalog paczki, ale że typedscript nie zawiera base dir to dupa na razie
                        File parentScriptsLocation = stackDescription.getStackResources().getStackDirectory();
                        String parentName = x.getName();
                        return execute(x, composeModel, stackDescription,
                                parentScriptsLocation, parentName,
                                environment, withOptional).stream();
                    });
            provisionersExtra = Streams.concat(fromGlobal, fromStacks, fromEnv).collect(Collectors.toList());
        } else {
            provisionersExtra = Collections.emptyList();
        }
        return new StackCommand(commandType, composeModel, stackDescription, provisionersExtra, environment);
    }

    private ExtensionContext getExtensionContext() {
        return extensionContext;
    }

    private List<NamedExecutable> execute(NamedExecutable executor,
                                          ComposeFileModel composeModel,
                                          StackDescription stackDescription,
                                          File parentScriptBaseLocation, String parentScriptName,
                                          Map<String, String> environment, boolean withOptional) {
        EnricherExecutableParams.Provisioners provisioners = new EnricherExecutableParams.Provisioners(extensionContext,
                parentScriptBaseLocation, parentScriptName);
        EnricherExecutableParams params = new EnricherExecutableParams(
                LOGGER,
                stackDescription,
                composeModel,
                environment,
                stack, withOptional,
                provisioners
        );
        Map<String, Object> ctxt = params.toMap();
        LOGGER.info("Enriching with {}.", executor.getName());
        executor.execute(ctxt);
        return provisioners;
    }

    private StackOperations buildOperations(Stream<StackCommand> stackCommandStream) {
        StackOperations.Builder builder = StackOperations.builder(stack.getEnvironment());
        stackCommandStream.forEach(builder::add);
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

    private StackCommand.Type getCommandType(StackDescription stackDescription) {
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
