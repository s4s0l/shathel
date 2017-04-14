package org.s4s0l.shathel.commons.scripts.vaagrant

import org.s4s0l.shathel.commons.remoteswarm.ProcessorCommand
import org.s4s0l.shathel.commons.remoteswarm.RemoteEnvironmentPackageContext
import org.s4s0l.shathel.commons.scripts.NamedExecutable
import org.s4s0l.shathel.commons.scripts.TypedScript
import org.s4s0l.shathel.commons.scripts.ansible.AnsibleScriptContext

/**
 * @author Marcin Wielgus
 */
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

        Optional<ProcessorCommand> command = ProcessorCommand.toCommand(context.get("command") ?: ProcessorCommand.APPLY.toString())
        RemoteEnvironmentPackageContext econtext = (RemoteEnvironmentPackageContext) context.get("context")
        File workingDir = script.scriptFileLocation.get().getParentFile()
        Map<String, String> env = (Map<String, String>) context.get("env")
        env << [
                "VAGRANT_DOTFILE_PATH": econtext.settingsDirectory.absolutePath,
                "VAGRANT_VAGRANTFILE" : script.scriptFileLocation.get().getName()
        ]

        if (command.isPresent()) {

            switch (command.get()) {
                case ProcessorCommand.APPLY:
                    env << ["OUTPUT": vagrant.up(workingDir, env)]
                    break
                case ProcessorCommand.STOP:
                    env << ["OUTPUT": vagrant.halt(workingDir, env)]
                    break
                case ProcessorCommand.DESTROY:
                    env << ["OUTPUT": vagrant.destroy(workingDir, env)]
                    break
                case ProcessorCommand.START:
                    env << ["OUTPUT": vagrant.up(workingDir, env)]
                    break
                case ProcessorCommand.STARTED:
                    def status = vagrant.status(workingDir, env)
                    env << ["RESULT": "${status.find { it.value != "running" } == null}"]
                    break
                case ProcessorCommand.INITED:
                    def status = vagrant.status(workingDir, env)
                    env << ["RESULT": "${status.find { it.value == "not created" } == null}"]
                    break

            }
        } else {
            if (context.get("command") == null) {
                throw new RuntimeException("No command found for vagrant")
            }
            env << ["OUTPUT": vagrant.run(workingDir, env, context.get("command"))]
        }


    }

}
