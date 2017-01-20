package org.s4s0l.shathel.commons.core.enricher;

import groovy.util.Eval;
import org.codehaus.groovy.runtime.ResourceGroovyMethods;
import org.s4s0l.shathel.commons.core.model.ComposeFileModel;
import org.s4s0l.shathel.commons.core.stack.StackDescription;
import org.s4s0l.shathel.commons.core.stack.StackEnricherDefinition;
import org.s4s0l.shathel.commons.core.stack.StackProvisionerDefinition;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Matcin Wielgus
 */
public class GroovishEnricher implements Enricher {
    public GroovishEnricher(StackEnricherDefinition definition) {
        this.definedIn = definition;
    }

    @Override
    public List<StackProvisionerDefinition> enrich(StackDescription stack,
                                                   ComposeFileModel shathelStackFileModel) {
        String script;
        try {
            script = definedIn.getInline() != null ? definedIn.getInline() :
                    ResourceGroovyMethods.getText(getEnricherFile(stack));
        } catch (IOException e) {
            throw new RuntimeException("Unable to find Enricher script", e);
        }

        final Map values = new HashMap();
        values.put("stack", stack);
        values.put("compose", shathelStackFileModel);
        Eval.me("s", values, script);
        return new ArrayList();
    }

    private File getEnricherFile(StackDescription stack) {
        return new File(stack.getStackResources().getStackDirectory(), "enrichers/" + definedIn.getName() + ".groovy");
    }

    private final StackEnricherDefinition definedIn;
}
