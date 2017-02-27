package org.s4s0l.shathel.commons.core.stack;

import org.s4s0l.shathel.commons.core.model.StackFileModel;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Matcin Wielgus
 */
public class StackDescriptionImpl implements StackDescription {

    private final StackFileModel fileModel;
    private final StackResources stackResources;

    public StackDescriptionImpl(StackFileModel fileModel, StackResources stackResources) {
        this.fileModel = fileModel;
        this.stackResources = stackResources;
    }

    @Override
    public String getName() {
        return fileModel.getName();
    }

    @Override
    public String getVersion() {
        return fileModel.getVersion();
    }

    @Override
    public String getGroup() {
        return fileModel.getGroup();
    }

    @Override
    public String getDeployName() {
        return fileModel.getDeployName();
    }

    @Override
    public String getGav() {
        return fileModel.getGav();
    }

    @Override
    public StackResources getStackResources() {
        return stackResources;
    }

    @Override
    public List<StackReference> getDependencies() {
        return fileModel.getDependencies().stream()
                .map(map -> new StackReference(map.get("group"), map.get("name"), map.get("version")))
                .collect(Collectors.toList());

    }

    @Override
    public List<StackProvisionerDefinition> getPreProvisioners() {
        return fileModel.getPreProvisioners().stream()
                .map(map -> new StackProvisionerDefinition(this,"pre-provisioners",
                        map.get("name"),
                        map.get("inline"),
                        map.get("type"))
                ).collect(Collectors.toList());
    }

    @Override
    public List<StackProvisionerDefinition> getPostProvisioners() {
        return fileModel.getPostProvisioners().stream()
                .map(map -> new StackProvisionerDefinition(this,"post-provisioners",
                        map.get("name"),
                        map.get("inline"),
                        map.get("type"))
                ).collect(Collectors.toList());
    }

    @Override
    public List<StackEnricherDefinition> getEnricherDefinitions() {
        return fileModel.getEnrichers().stream()
                .map(map -> new StackEnricherDefinition(this,
                        map.get("name"),
                        map.get("inline"),
                        map.get("type"),
                        StackEnricherDefinition.Target.valueOf(map.get("target"))
                )).collect(Collectors.toList());
    }

    @Override
    public boolean isDependantOn(StackReference reference) {
        return getDependencies().stream()
                .filter(d ->
                        d.getName().equals(reference.getName()) &&
                                d.getGroup().equals(reference.getGroup()))
                .findAny().isPresent();
    }
}
