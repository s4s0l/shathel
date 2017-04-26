package org.s4s0l.shathel.commons.scripts.vaagrant

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import org.s4s0l.shathel.commons.core.environment.EnvironmentContext
import org.s4s0l.shathel.commons.remoteswarm.ProcessorCommand
import org.s4s0l.shathel.commons.utils.ExecutableResults
import org.s4s0l.shathel.commons.scripts.NamedExecutable
import org.s4s0l.shathel.commons.scripts.TypedScript

/**
 * @author Marcin Wielgus
 */
@TypeChecked
@CompileStatic
class VagrantExecutable implements NamedExecutable {
    private final TypedScript script
    private final VagrantWrapper vagrant

    VagrantExecutable(TypedScript script, VagrantWrapper vagrant) {
        this.script = script
        this.vagrant = vagrant
    }

    @Override
    String getName() {
        return script.getScriptName()
    }

    @Override
    void execute(Map<String, Object> context) {
        if (context.get("result") == null) {
            context.put("result", new ExecutableResults())
        }
        ExecutableResults results = context.get("result") as ExecutableResults
        Optional<ProcessorCommand> command = ProcessorCommand.toCommand(context.get("command") as String ?: ProcessorCommand.APPLY.toString())
        EnvironmentContext econtext = (EnvironmentContext) context.get("context")
        File workingDir = script.scriptFileLocation.get().getParentFile()
        Map<String, String> env = (Map<String, String>) context.get("env")
        env.putAll([
                "VAGRANT_DOTFILE_PATH": econtext.settingsDirectory.absolutePath,
                "VAGRANT_VAGRANTFILE" : script.scriptFileLocation.get().getName(),
        ])
        boolean localVagrant = econtext.environmentDescription?.getParameterAsBoolean("useglobalvagrant")?.orElse(true)
        if (!localVagrant) {
            env.putAll([
                    "VAGRANT_HOME": new File(econtext.dependencyCacheDirectory, ".vagrant.d").absolutePath
            ])
        }

        if (command.isPresent()) {

            switch (command.get()) {
                case ProcessorCommand.APPLY:
                    results.output = vagrant.up(workingDir, env)
                    break
                case ProcessorCommand.STOP:
                    results.output = vagrant.halt(workingDir, env)
                    break
                case ProcessorCommand.DESTROY:
                    results.output = vagrant.destroy(workingDir, env)
                    break
                case ProcessorCommand.START:
                    results.output = vagrant.up(workingDir, env)
                    break
                case ProcessorCommand.STARTED:
                    def status = vagrant.status(workingDir, env)
                    results.output = status
                    results.status = status.find { it.value != "running" } == null
                    break
                case ProcessorCommand.INITED:
                    def status = vagrant.status(workingDir, env)
                    results.output = status
                    results.status = status.find {
                        it.value == "not created"
                    } == null
                    break

            }
        } else {
            if (context.get("command") == null) {
                throw new RuntimeException("No command found for vagrant")
            }
            results.output = vagrant.run(workingDir, env, (String) context.get("command"))
        }


    }

}
