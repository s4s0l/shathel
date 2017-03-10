package org.s4s0l.shathel.commons.core.stack;

import java.util.List;

/**
 * @author Marcin Wielgus
 */
public interface StackDescription {
    String getName();

    String getVersion();

    String getGroup();

    String getDeployName();

    default StackReference getReference() {
        return new StackReference(getGroup(), getName(), getVersion());
    }

    StackResources getStackResources();

    List<StackDependency> getDependencies();


    List<StackProvisionerDefinition> getPreProvisioners();

    List<StackProvisionerDefinition> getPostProvisioners();

    List<StackEnricherDefinition> getEnricherDefinitions();

    boolean isDependantOn(StackReference reference, boolean includeOptional);

    String getGav();
}
