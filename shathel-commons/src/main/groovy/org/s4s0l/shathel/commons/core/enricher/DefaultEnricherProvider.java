package org.s4s0l.shathel.commons.core.enricher;

import org.s4s0l.shathel.commons.core.stack.StackEnricherDefinition;

/**
 * @author Matcin Wielgus
 */
public class DefaultEnricherProvider implements EnricherProvider {
    @Override
    public Enricher getEnricher(StackEnricherDefinition enricherDefinition) {
        if (!enricherDefinition.getType().equals("groovy"))
            throw new UnsupportedOperationException("Only groovy enrichers");
        return new GroovishEnricher(enricherDefinition);

    }

}
