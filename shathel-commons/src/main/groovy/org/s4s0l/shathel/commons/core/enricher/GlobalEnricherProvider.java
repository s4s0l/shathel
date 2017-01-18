package org.s4s0l.shathel.commons.core.enricher;

import org.s4s0l.shathel.commons.utils.ExtensionInterface;

import java.util.List;

/**
 * @author Matcin Wielgus
 */
public interface GlobalEnricherProvider  extends ExtensionInterface {
    List<Enricher> getGlobalEnrichers();
}
