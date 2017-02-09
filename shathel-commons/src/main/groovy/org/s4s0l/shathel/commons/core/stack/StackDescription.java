package org.s4s0l.shathel.commons.core.stack;

import java.util.List;

/**
 * @author Matcin Wielgus
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

    List<StackReference> getDependencies();

    List<StackProvisionerDefinition> getPreProvisioners();

    List<StackProvisionerDefinition> getPostProvisioners();

    List<StackEnricherDefinition> getEnricherDefinitions();

    boolean isDependantOn(StackReference reference);


    String getGav();
}
