package org.s4s0l.shathel.commons.scripts

/**
 * @author Matcin Wielgus
 */
class GroovyExecutor {

    Object execute(File scriptFile, Map<String, Object> variables) {
        return execute(scriptFile.text, variables)
    }

    Object execute(String scriptFile, Map<String, Object> variables) {
        GroovyShell shell = new GroovyShell()
        Script scrpt = shell.parse(scriptFile)
        Binding binding = new Binding()
        variables.each {
            binding.setVariable(it.key, it.value)
        }
        scrpt.setBinding(binding)
        return scrpt.run()
    }


}
