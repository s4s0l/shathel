package org.s4s0l.shathel.commons.core.enricher;

import org.s4s0l.shathel.commons.core.stack.StackEnricherDefinition;
import org.s4s0l.shathel.commons.utils.ExtensionInterface;

/**
 * @author Matcin Wielgus
 */
public interface EnricherProvider extends ExtensionInterface {

    Enricher getEnricher(StackEnricherDefinition enricherDefinition);

}
