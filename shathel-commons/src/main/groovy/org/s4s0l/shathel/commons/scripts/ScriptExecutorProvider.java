package org.s4s0l.shathel.commons.scripts;

import org.s4s0l.shathel.commons.utils.ExtensionContext;
import org.s4s0l.shathel.commons.utils.ExtensionInterface;

import java.util.Optional;

/**
 * @author Marcin Wielgus
 */
public interface ScriptExecutorProvider extends ExtensionInterface {

    Optional<NamedExecutable> findExecutable(ExtensionContext cntext,TypedScript typedScript);


    static Optional<NamedExecutable> findExecutor(ExtensionContext cntext, TypedScript typedScript) {
        return cntext.lookupAll(ScriptExecutorProvider.class)
                .map(it -> it.findExecutable(cntext,typedScript))
                .filter(it -> it.isPresent())
                .findFirst().orElse(Optional.empty());
    }
}
