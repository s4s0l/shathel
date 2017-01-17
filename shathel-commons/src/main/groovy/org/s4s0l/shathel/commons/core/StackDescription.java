package org.s4s0l.shathel.commons.core;

import org.s4s0l.shathel.commons.files.model.ShathelStackFileModel;

import java.io.File;
import java.util.List;

/**
 * @author Matcin Wielgus
 */
public interface StackDescription {
    String getName();

    String getVersion();

    String getGroup();

    default StackReference getReference() {
        return new StackReference(getName(), getGroup(), getVersion());
    }
    StackResources getStackResources();

    List<StackReference> getDependencies();

    List<StackProvisionerDefinition> getProvisioners();

    List<StackEnricherDefinition> getEnricherDefinitions();

    boolean isDependantOn(StackReference reference);


}
