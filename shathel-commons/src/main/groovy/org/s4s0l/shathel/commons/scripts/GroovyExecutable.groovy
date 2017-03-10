package org.s4s0l.shathel.commons.scripts

import org.slf4j.LoggerFactory

/**
 * @author Marcin Wielgus
 */
class GroovyExecutable implements Executable {
    private final TypedScript script;

    GroovyExecutable(TypedScript script) {
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
        binding.setVariable("LOGGER", LoggerFactory.getLogger(GroovyExecutable.class))
        scrpt.setBinding(binding)
        return scrpt.run()
    }

    @Override
    Object execute(Map<String, Object> context) {
        return execute(script.getScriptContents(), context)
    }
}
