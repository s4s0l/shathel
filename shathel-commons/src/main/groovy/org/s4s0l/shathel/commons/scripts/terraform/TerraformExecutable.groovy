package org.s4s0l.shathel.commons.scripts.terraform

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import org.s4s0l.shathel.commons.remoteswarm.ProcessorCommand
import org.s4s0l.shathel.commons.remoteswarm.RemoteEnvironmentPackageContext
import org.s4s0l.shathel.commons.scripts.ExecutableResults
import org.s4s0l.shathel.commons.scripts.NamedExecutable
import org.s4s0l.shathel.commons.scripts.TypedScript
import org.s4s0l.shathel.commons.scripts.ansible.AnsibleScriptContext
import org.s4s0l.shathel.commons.scripts.vaagrant.VagrantWrapper

/**
 * @author Marcin Wielgus
 */
@TypeChecked
@CompileStatic
class TerraformExecutable implements NamedExecutable {
    private final TypedScript script
    private final TerraformWrapper terraform

    TerraformExecutable(TypedScript script, TerraformWrapper terraform) {
        this.script = script
        this.terraform = terraform
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
        File workingDir = script.scriptFileLocation.get().getParentFile()
        Map<String, String> env = (Map<String, String>) context.get("env")
        TerraformScriptContext tsc = (TerraformScriptContext) context.get("terraform")

        if (command.isPresent()) {

            switch (command.get()) {
                case ProcessorCommand.APPLY:
                    results.output = terraform.apply(workingDir, tsc.stateFile, script.scriptFileLocation.get(), env)
                    Map<String, String> outVars = terraform.output(workingDir, tsc.stateFile, script.scriptFileLocation.get(), env)
                    env.putAll(outVars)
                    break
                case ProcessorCommand.STOP:
                    break
                case ProcessorCommand.DESTROY:
                    results.output = terraform.destroy(workingDir, tsc.stateFile, script.scriptFileLocation.get(), env)
                    break
                case ProcessorCommand.START:
                    break
                case ProcessorCommand.STARTED:
                    Map<String, Integer> x = terraform.plan(workingDir, tsc.stateFile, script.scriptFileLocation.get(), env)
                    results.status = x.get("allChanges") == 0
                    break
                case ProcessorCommand.INITED:
                    Map<String, Integer> x = terraform.plan(workingDir, tsc.stateFile, script.scriptFileLocation.get(), env)
                    results.status = x.get("allChanges") == 0
                    break

            }
        } else {
            if (context.get("command") == null) {
                throw new RuntimeException("No command found for vagrant")
            }
            if (context.get("command") == "output") {
                Map<String, String> outVars = terraform.output(workingDir, tsc.stateFile, script.scriptFileLocation.get(), env)
                env.putAll(outVars)
            } else {
                results.output = terraform.run(workingDir, tsc.stateFile, script.scriptFileLocation.get(), env,context.get("command") as String)
            }

        }


    }

}
