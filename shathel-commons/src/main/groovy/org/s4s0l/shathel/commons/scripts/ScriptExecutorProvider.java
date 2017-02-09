package org.s4s0l.shathel.commons.scripts;

import org.s4s0l.shathel.commons.utils.ExtensionContext;
import org.s4s0l.shathel.commons.utils.ExtensionInterface;

import java.util.Optional;

/**
 * @author Matcin Wielgus
 */
public interface ScriptExecutorProvider extends ExtensionInterface {

    Optional<Executor> findExecutor(TypedScript typedScript);


    static Optional<Executor> findExecutor(ExtensionContext cntext, TypedScript typedScript) {
        return cntext.lookupAll(ScriptExecutorProvider.class)
                .map(it -> it.findExecutor(typedScript))
                .filter(it -> it.isPresent())
                .findFirst().orElse(Optional.empty());
    }
}
