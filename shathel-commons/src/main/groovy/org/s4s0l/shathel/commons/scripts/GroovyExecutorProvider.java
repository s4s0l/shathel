package org.s4s0l.shathel.commons.scripts;

import java.util.Optional;

/**
 * @author Matcin Wielgus
 */
public class GroovyExecutorProvider implements ScriptExecutorProvider {

    @Override
    public Optional<Executor> findExecutor(TypedScript typedScript) {
        if ("groovy".equals(typedScript.getType())) {
            return Optional.of(new GroovyExecutor(typedScript));
        } else {
            return Optional.empty();
        }
    }
}
