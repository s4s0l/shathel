package org.s4s0l.shathel.commons.core.stack;

import org.s4s0l.shathel.commons.core.model.StackFileModel;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Marcin Wielgus
 */
public class StackDescriptionImpl implements StackDescription {

    private final StackFileModel fileModel;
    private final StackResources stackResources;
    private final SolutionStackDesctiption solutionStackDesctiption;

    public StackDescriptionImpl(StackFileModel fileModel, StackResources stackResources, SolutionStackDesctiption solutionStackDesctiption) {
        this.fileModel = fileModel;
        this.stackResources = stackResources;
        this.solutionStackDesctiption = solutionStackDesctiption;
    }

    @Override
    public String getName() {
        return getReference().getName();
    }

    @Override
    public String getVersion() {
        return getReference().getVersion();
    }

    @Override
    public String getGroup() {
        return getReference().getGroup();
    }

    @Override
    public String getDeployName() {
        return fileModel.getDeployName() == null ? getName() : fileModel.getDeployName();
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
    public List<StackDependency> getDependencies() {
        return fileModel.getDependencies().stream()
                .map(map -> new StackDependency(
                        new StackReference((String) map.get("gav")),
                        (Boolean) map.get("optional"),
                        (Map<String, String>) map.get("envs")))
                .collect(Collectors.toList());

    }


    @Override
    public List<StackProvisionerDefinition> getPreProvisioners() {
        return fileModel.getPreProvisioners().stream()
                .map(map -> new StackProvisionerDefinition(this, "pre-provisioners",
                        map.get("name"),
                        map.get("inline"),
                        map.get("type"))
                ).collect(Collectors.toList());
    }

    @Override
    public List<StackProvisionerDefinition> getPostProvisioners() {
        return fileModel.getPostProvisioners().stream()
                .map(map -> new StackProvisionerDefinition(this, "post-provisioners",
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
    public boolean isDependantOn(StackReference reference, boolean includeOptional) {
        return getDependencies().stream()
                .filter(d -> includeOptional || !d.isOptional())
                .filter(d ->
                        d.getStackReference().isSameStack(reference))
                .findAny().isPresent();
    }

    @Override
    public String toString() {
        return "StackDescriptionImpl{" +
                "group='" + getGroup() + '\'' +
                ", name='" + getName() + '\'' +
                ", version='" + getVersion() + '\'' +
                '}';
    }

    @Override
    public Map<String, String> getEnvs() {
        return solutionStackDesctiption.getEnvironments();
    }

    @Override
    public Map<String, String> getMandatoryEnvs() {
        return fileModel.getMandatoryEnvs();
    }
}
