package org.s4s0l.shathel.commons.scripts

import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * @author Matcin Wielgus
 */
class GroovyExecutor implements Executor {
    private final TypedScript script;

    GroovyExecutor(TypedScript script) {
        this.script = script
    }

    Object execute(File scriptFile, Map<String, Object> variables) {
        return execute(scriptFile.text, variables)
    }

    Object execute(String scriptFile, Map<String, Object> variables) {
        GroovyShell shell = new GroovyShell()
        def scrpt = shell.parse(scriptFile)
        Binding binding = new Binding()
        variables.each {
            binding.setVariable(it.key, it.value)
        }
        binding.setVariable("LOGGER", LoggerFactory.getLogger(GroovyExecutor.class))
        scrpt.setBinding(binding)
        return scrpt.run()
    }

    @Override
    Object execute(Map<String, Object> context) {
        return execute(script.getScriptContents(), context)
    }
}
