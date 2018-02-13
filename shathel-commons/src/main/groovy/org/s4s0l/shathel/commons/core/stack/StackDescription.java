package org.s4s0l.shathel.commons.core.stack;

import org.s4s0l.shathel.commons.core.EnvironmentVariabllesContainer;

import java.util.List;
import java.util.Map;

/**
 * @author Marcin Wielgus
 */
public interface StackDescription extends EnvironmentVariabllesContainer {
    String getName();

    String getVersion();

    String getGroup();

    String getDeployName();

    default StackReference getReference() {
        return new StackReference(getGav());
    }

    StackResources getStackResources();

    List<StackDependency> getDependencies();

    List<StackProvisionerDefinition> getPreProvisioners();

    List<StackProvisionerDefinition> getPostProvisioners();

    List<StackEnricherDefinition> getEnricherDefinitions();

    boolean isDependantOn(StackReference reference, boolean includeOptional);

    String getGav();

    Map<String,String> getMandatoryEnvs();
}
