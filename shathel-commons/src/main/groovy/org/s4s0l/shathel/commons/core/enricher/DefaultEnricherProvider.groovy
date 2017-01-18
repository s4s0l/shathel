package org.s4s0l.shathel.commons.core.enricher

import org.s4s0l.shathel.commons.core.stack.StackEnricherDefinition

/**
 * @author Matcin Wielgus
 */
class DefaultEnricherProvider implements EnricherProvider {
    @Override
    Enricher getEnricher(StackEnricherDefinition enricherDefinition) {
        if (enricherDefinition.type != "groovy")
            throw UnsupportedOperationException("Only groovy enrichers");
        return new GroovishEnricher(enricherDefinition);

    }
}