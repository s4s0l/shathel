package org.s4s0l.shathel.commons.scripts.terraform

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import org.s4s0l.shathel.commons.core.environment.EnvironmentContext
import org.s4s0l.shathel.commons.remoteswarm.ProcessorCommand
import org.s4s0l.shathel.commons.utils.ExecutableResults
import org.s4s0l.shathel.commons.scripts.NamedExecutable
import org.s4s0l.shathel.commons.scripts.TypedScript
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * @author Marcin Wielgus
 */
@TypeChecked
@CompileStatic
class TerraformExecutable implements NamedExecutable {
    private static final Logger LOGGER = LoggerFactory.getLogger(TerraformExecutable.class);
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
        EnvironmentContext econtext = (EnvironmentContext) context.get("context")
        Map<String, String> env = (Map<String, String>) context.get("env")
        env.putAll([
                TF_INPUT   : "0",
                TF_LOG     : "TRACE",
                TF_LOG_PATH: "${econtext.tempDirectory.absolutePath}/terraform.log".toString()

        ])
        TerraformScriptContext tsc = (TerraformScriptContext) context.get("terraform")

        if (command.isPresent()) {

            switch (command.get()) {
                case ProcessorCommand.APPLY:
                    results.output = terraform.apply(workingDir, tsc.stateFile, script.scriptFileLocation.get(), env)
                    Map<String, String> outVars = terraform.output(workingDir, tsc.stateFile, script.scriptFileLocation.get(), env)
                    env.putAll(outVars)
                    break
                case ProcessorCommand.STOP:
                    LOGGER.warn("Terraform ${command.get()} is not implemented - results may be missleading!")
                    break
                case ProcessorCommand.DESTROY:
                    results.output = terraform.destroy(workingDir, tsc.stateFile, script.scriptFileLocation.get(), env)
                    break
                case ProcessorCommand.START:
                    LOGGER.warn("Terraform ${command.get()} is not implemented - results may be missleading!")
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
                results.output = terraform.run(workingDir, tsc.stateFile, script.scriptFileLocation.get(), env, context.get("command") as String)
            }

        }


    }


}
