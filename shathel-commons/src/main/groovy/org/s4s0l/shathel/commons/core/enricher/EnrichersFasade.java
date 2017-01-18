package org.s4s0l.shathel.commons.core.enricher;

import org.s4s0l.shathel.commons.core.stack.StackEnricherDefinition;
import org.s4s0l.shathel.commons.utils.ExtensionContext;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Matcin Wielgus
 */
public class EnrichersFasade implements EnricherProvider, GlobalEnricherProvider {
    private final ExtensionContext extensionContext;


    public EnrichersFasade(ExtensionContext extensionContext) {
        this.extensionContext = extensionContext;
    }

    @Override
    public Enricher getEnricher(StackEnricherDefinition enricherDefinition) {
        return extensionContext.lookupOne(EnricherProvider.class).get()
                .getEnricher(enricherDefinition);
    }

    @Override
    public List<Enricher> getGlobalEnrichers() {
        return extensionContext.lookupAll(GlobalEnricherProvider.class)
                .map(it -> it.getGlobalEnrichers())
                .flatMap(it -> it.stream())
                .collect(Collectors.toList());
    }
}
