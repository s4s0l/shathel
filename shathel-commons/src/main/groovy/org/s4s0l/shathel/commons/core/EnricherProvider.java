package org.s4s0l.shathel.commons.core;

import java.util.List;

/**
 * @author Matcin Wielgus
 */
public interface EnricherProvider {
    Enricher getEnricher(StackEnricherDefinition enricherDefinition);

    List<Enricher> getGlobalEnrichers();
}
