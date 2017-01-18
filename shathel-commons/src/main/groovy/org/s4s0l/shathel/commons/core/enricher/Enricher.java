package org.s4s0l.shathel.commons.core.enricher;

import org.s4s0l.shathel.commons.core.model.ComposeFileModel;
import org.s4s0l.shathel.commons.core.stack.StackDescription;
import org.s4s0l.shathel.commons.core.stack.StackProvisionerDefinition;

import java.util.List;

/**
 * @author Matcin Wielgus
 */
public interface Enricher {
    List<StackProvisionerDefinition> enrich(StackDescription stack, ComposeFileModel shathelStackFileModel);
}
