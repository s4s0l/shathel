package org.s4s0l.shathel.commons.core;

import groovy.lang.Tuple;
import groovy.util.MapEntry;
import org.s4s0l.shathel.commons.core.enricher.Enricher;
import org.s4s0l.shathel.commons.core.enricher.EnrichersFasade;
import org.s4s0l.shathel.commons.core.environment.StackIntrospection;
import org.s4s0l.shathel.commons.core.environment.StackIntrospectionProvider;
import org.s4s0l.shathel.commons.core.model.ComposeFileModel;
import org.s4s0l.shathel.commons.core.provision.StackCommand;
import org.s4s0l.shathel.commons.core.stack.StackDescription;
import org.s4s0l.shathel.commons.core.stack.StackEnricherDefinition;
import org.s4s0l.shathel.commons.core.stack.StackProvisionerDefinition;
import org.s4s0l.shathel.commons.core.stack.StackTreeDescription;
import org.s4s0l.shathel.commons.utils.StreamUtils;
import org.s4s0l.shathel.commons.utils.VersionComparator;

import java.util.AbstractMap;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Matcin Wielgus
 */
public class StackOperationsFactory {
    private final StackTreeDescription descriptionTree;
    private final StackIntrospectionProvider introspectionProvider;
    private final EnrichersFasade enricherProvider;

    public StackOperationsFactory(StackTreeDescription descriptionTree,
                                  StackIntrospectionProvider introspectionProvider,
                                  EnrichersFasade enricherProvider) {
        this.descriptionTree = descriptionTree;
        this.introspectionProvider = introspectionProvider;
        this.enricherProvider = enricherProvider;
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
        ComposeFileModel shathelStackFileModel = stack.getStackResources().getComposeFileModel();
        List<StackEnricherDefinition> enricherDefinitions = getEnricherDefinitions(stack);
        List<StackProvisionerDefinition> allProvisioners = new ArrayList<>();
        allProvisioners.addAll(stack.getProvisioners());
        for (StackEnricherDefinition enricherDefinition : enricherDefinitions) {
            Enricher enricher = enricherProvider.getEnricher(enricherDefinition);
            allProvisioners.addAll(enricher.enrich(stack, shathelStackFileModel));
        }
        List<Enricher> globalEnrichers = enricherProvider.getGlobalEnrichers();
        for (Enricher enricher : globalEnrichers) {
            allProvisioners.addAll(enricher.enrich(stack, shathelStackFileModel));
        }
        return new StackCommand(commandType, shathelStackFileModel, stack, allProvisioners);
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
        Optional<StackIntrospection> introspection = introspectionProvider.getIntrospection(stack.getReference());
        StackCommand.Type commandType = StackCommand.Type.NOOP;
        if (introspection.isPresent()) {
            if (new VersionComparator().compare(introspection.get().getReference().getVersion(), stack.getVersion()) < 0) {
                commandType = StackCommand.Type.UPDATE;
            }
        } else {
            commandType = StackCommand.Type.START;
        }
        return commandType;
    }


}
