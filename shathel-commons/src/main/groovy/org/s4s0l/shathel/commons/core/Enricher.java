package org.s4s0l.shathel.commons.core;

import org.s4s0l.shathel.commons.files.model.ComposeFileModel;
import org.s4s0l.shathel.commons.files.model.ShathelStackFileModel;

import java.util.List;

/**
 * @author Matcin Wielgus
 */
public interface Enricher {
    List<StackProvisionerDefinition> enrich(StackDescription stack, ComposeFileModel shathelStackFileModel);
}
