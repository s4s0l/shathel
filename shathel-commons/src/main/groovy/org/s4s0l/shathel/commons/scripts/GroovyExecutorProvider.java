package org.s4s0l.shathel.commons.scripts;

import java.util.Optional;

/**
 * @author Marcin Wielgus
 */
public class GroovyExecutorProvider implements ScriptExecutorProvider {

    @Override
    public Optional<NamedExecutable> findExecutor(TypedScript typedScript) {
        if ("groovy".equals(typedScript.getType())) {
            return Optional.of(new GroovyExecutable(typedScript));
        } else {
            return Optional.empty();
        }
    }
}
