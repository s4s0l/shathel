package org.s4s0l.shathel.commons.core;

import org.s4s0l.shathel.commons.files.model.ShathelStackFileModel;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Matcin Wielgus
 */
public class StackDescriptionImpl implements StackDescription {

    private final ShathelStackFileModel fileModel;
    private final StackResources stackResources;

    public StackDescriptionImpl(ShathelStackFileModel fileModel, StackResources stackResources) {
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
    public StackResources getStackResources() {
        return stackResources;
    }

    @Override
    public List<StackReference> getDependencies() {
        return fileModel.getDependencies().stream()
                .map(map -> new StackReference(map.get("name"), map.get("group"), map.get("version")))
                .collect(Collectors.toList());

    }

    @Override
    public List<StackProvisionerDefinition> getProvisioners() {
        return Collections.EMPTY_LIST;
    }

    @Override
    public List<StackEnricherDefinition> getEnricherDefinitions() {
        return Collections.EMPTY_LIST;
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
