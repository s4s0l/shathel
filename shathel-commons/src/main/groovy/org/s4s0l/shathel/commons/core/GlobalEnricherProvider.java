package org.s4s0l.shathel.commons.core;

import org.s4s0l.shathel.commons.scripts.Executor;
import org.s4s0l.shathel.commons.utils.ExtensionContext;
import org.s4s0l.shathel.commons.utils.ExtensionInterface;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Matcin Wielgus
 */
public interface GlobalEnricherProvider extends ExtensionInterface {
    List<Executor> getGlobalEnrichers();

    static List<Executor> getGlobalEnrichers(ExtensionContext extensionContext) {
        return extensionContext.lookupAll(GlobalEnricherProvider.class)
                .map(it -> it.getGlobalEnrichers())
                .flatMap(it -> it.stream())
                .collect(Collectors.toList());
    }
}
