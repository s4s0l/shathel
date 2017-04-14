package org.s4s0l.shathel.commons.scripts.packer

import org.s4s0l.shathel.commons.remoteswarm.ProcessorCommand
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
        Map<String, String> env = (Map<String, String>) context.get("env")
        Optional<ProcessorCommand> command = ProcessorCommand.toCommand(context.get("command") as String ?: ProcessorCommand.APPLY.toString())
        if (command.isPresent()) {
            throw new UnsupportedOperationException("Not yet!")
        }
        results.output = packer.run(workingDir, context.get("command") as String, env)
    }
}
