package org.s4s0l.shathel.commons.scripts.vaagrant

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import org.s4s0l.shathel.commons.remoteswarm.ProcessorCommand
import org.s4s0l.shathel.commons.remoteswarm.RemoteEnvironmentPackageContext
import org.s4s0l.shathel.commons.scripts.NamedExecutable
import org.s4s0l.shathel.commons.scripts.TypedScript
import org.s4s0l.shathel.commons.utils.ExecutableResults

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
    TypedScript getScript() {
        return script
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
        RemoteEnvironmentPackageContext econtext = (RemoteEnvironmentPackageContext) context.get("context")
        File workingDir = script.scriptFileLocation.get().getParentFile()
        Map<String, String> env = (Map<String, String>) context.get("env")
        env.putAll([
                "VAGRANT_DOTFILE_PATH": econtext.settingsDirectory.absolutePath,
                "VAGRANT_VAGRANTFILE" : script.scriptFileLocation.get().getName(),
        ])

        boolean globalVagrant = econtext.getEnvironmentParameterAsBoolean("useglobalvagrant").orElse(false)
        if (!globalVagrant) {
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
