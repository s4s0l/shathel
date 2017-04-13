package org.s4s0l.shathel.commons.scripts.groovy

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import org.s4s0l.shathel.commons.scripts.NamedExecutable
import org.s4s0l.shathel.commons.scripts.TypedScript
import org.slf4j.LoggerFactory

/**
 * @author Marcin Wielgus
 */
@TypeChecked
@CompileStatic
class GroovyExecutable implements NamedExecutable {
    private final TypedScript script;

    GroovyExecutable(TypedScript script) {
        this.script = script
    }

    @Override
    String getName() {
        return script.getScriptName();
    }

    void execute(File scriptFile, Map<String, Object> variables) {
        execute(scriptFile.text, variables)
    }

    void execute(String scriptFile, Map<String, Object> variables) {
        GroovyShell shell = new GroovyShell()
        def scrpt = shell.parse(scriptFile)
        Binding binding = new Binding()
        variables.each {
            binding.setVariable(it.key, it.value)
        }
        binding.setVariable("LOGGER", LoggerFactory.getLogger(GroovyExecutable.class))
        scrpt.setBinding(binding)
        scrpt.run()
    }

    @Override
    void execute(Map<String, Object> context) {
        execute(script.getScriptContents(), context)
    }
}
