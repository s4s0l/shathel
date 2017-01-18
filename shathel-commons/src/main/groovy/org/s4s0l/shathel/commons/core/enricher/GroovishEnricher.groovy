package org.s4s0l.shathel.commons.core.enricher

import org.s4s0l.shathel.commons.core.model.ComposeFileModel
import org.s4s0l.shathel.commons.core.stack.StackDescription
import org.s4s0l.shathel.commons.core.stack.StackEnricherDefinition
import org.s4s0l.shathel.commons.core.stack.StackProvisionerDefinition

/**
 * @author Matcin Wielgus
 */
class GroovishEnricher implements Enricher {
    private final StackEnricherDefinition definedIn;

    GroovishEnricher(StackEnricherDefinition definition) {
        this.definedIn = definition
    }

    @Override
    List<StackProvisionerDefinition> enrich(StackDescription stack, ComposeFileModel shathelStackFileModel) {
        String script = definedIn.inline != null ? definedIn.inline :
                new File(stack.stackResources.stackDirectory, "enrichers/${definedIn.name}.groovy").text

        final Map values = new HashMap();
        values.put("stack", stack);
        values.put("compose", shathelStackFileModel);
        Eval.me("s",values, script);
        return []
    }
}
