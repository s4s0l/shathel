package org.s4s0l.shathel.commons.scripts.packer

import org.s4s0l.shathel.commons.core.environment.EnvironmentContext
import org.s4s0l.shathel.commons.remoteswarm.ProcessorCommand
import org.s4s0l.shathel.commons.remoteswarm.RemoteEnvironmentPackageContext
import org.s4s0l.shathel.commons.scripts.ExecutableResults
import org.s4s0l.shathel.commons.scripts.NamedExecutable
import org.s4s0l.shathel.commons.scripts.TypedScript
import org.s4s0l.shathel.commons.scripts.terraform.TerraformWrapper

/**
 * @author Marcin Wielgus
 */
class PackerExecutable implements NamedExecutable {
    private final TypedScript script
    private final PackerWrapper packer

    PackerExecutable(TypedScript script, PackerWrapper packer) {
        this.script = script
        this.packer = packer
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
        File workingDir = script.scriptFileLocation.get().getParentFile()
        EnvironmentContext econtext = (EnvironmentContext) context.get("context")
        Map<String, String> env = (Map<String, String>) context.get("env")
        env.putAll([
                PACKER_CACHE_DIR: "${econtext.dependencyCacheDirectory.absolutePath}/packer_cache".toString(),
                PACKER_LOG      : "1",
                PACKER_LOG_PATH : "${econtext.tempDirectory.absolutePath}/packer.log".toString(),
                PACKER_NO_COLOR : "true"
        ])

        new File("${econtext.dependencyCacheDirectory.absolutePath}/packer_cache").mkdirs()
//        new File("${econtext.tempDirectory.absolutePath}/packer_logs").mkdirs()


        Optional<ProcessorCommand> command = ProcessorCommand.toCommand(context.get("command") as String ?: ProcessorCommand.APPLY.toString())
        if (command.isPresent()) {
            switch (command.get()) {
                case ProcessorCommand.APPLY:
                    def extraVarsFile = new File(econtext.tempDirectory, "packer-extra-vars.json")
                    try {
                        extraVarsFile.text = "{" + env.collect {
                            "\t\"${it.key.toLowerCase()}\":\"${it.value}\""
                        }.join(",\n") + "}"
                        results.output = packer.run(workingDir, "build -var-file=${extraVarsFile.absolutePath} ${script.scriptFileLocation.get().absolutePath}", env)
                    } finally {
                        if (extraVarsFile.exists()) {
//                            extraVarsFile.delete()
                        }
                    }
                    break
                default:
                    throw new UnsupportedOperationException("Not yet!")
            }
        } else {
            if (context.get("command") == null) {
                throw new RuntimeException("No command found for vagrant")
            }
            results.output = packer.run(workingDir, context.get("command") as String, env)
        }

    }
}
