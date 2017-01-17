package org.s4s0l.shathel.commons.core;

import org.s4s0l.shathel.commons.files.model.ComposeFileModel;
import org.s4s0l.shathel.commons.utils.VersionComparator;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author Matcin Wielgus
 */
public class CommandScheduleFactory {
    private final StackDescriptionTree descriptionTree;
    private final StackIntrospectionProvider introspectionProvider;
    private final EnricherProvider enricherProvider;

    public CommandScheduleFactory(StackDescriptionTree descriptionTree, StackIntrospectionProvider introspectionProvider, EnricherProvider enricherProvider) {
        this.descriptionTree = descriptionTree;
        this.introspectionProvider = introspectionProvider;
        this.enricherProvider = enricherProvider;
    }

    public StartCommandSchedule createStartSchedule() {
        StartCommandSchedule.Builder builder = StartCommandSchedule.builder();


        descriptionTree.stream().forEach(stack -> {
            StackCommand.Type commandType = getCommandType(stack);
            if (commandType != StackCommand.Type.NOOP) {
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
                StackCommand cmd = new StackCommand(commandType, shathelStackFileModel, stack, allProvisioners);
                builder.add(cmd);

            }
        });
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
