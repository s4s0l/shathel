package org.s4s0l.shathel.commons.core;

import org.s4s0l.shathel.commons.scripts.Executable;
import org.s4s0l.shathel.commons.utils.ExtensionContext;
import org.s4s0l.shathel.commons.utils.ExtensionInterface;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Matcin Wielgus
 */
public interface GlobalEnricherProvider extends ExtensionInterface {
    List<Executable> getGlobalEnrichers();

    static List<Executable> getGlobalEnrichers(ExtensionContext extensionContext) {
        return extensionContext.lookupAll(GlobalEnricherProvider.class)
                .map(it -> it.getGlobalEnrichers())
                .flatMap(it -> it.stream())
                .collect(Collectors.toList());
    }
}
