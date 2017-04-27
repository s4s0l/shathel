package org.s4s0l.shathel.commons.remoteswarm

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import org.s4s0l.shathel.commons.core.environment.ExecutableApiFacade
import org.s4s0l.shathel.commons.utils.ExecutableResults
import org.s4s0l.shathel.commons.scripts.ScriptExecutorProvider
import org.s4s0l.shathel.commons.scripts.TypedScript
import org.s4s0l.shathel.commons.scripts.ansible.AnsibleScriptContext
import org.s4s0l.shathel.commons.scripts.terraform.TerraformScriptContext
import org.s4s0l.shathel.commons.utils.ExtensionContext
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * @author Marcin Wielgus
 */
@TypeChecked
@CompileStatic
class RemoteEnvironmentProcessorsFactory implements RemoteEnvironmentCallbackProcessors {
    private static
    final Logger LOGGER = LoggerFactory.getLogger(RemoteEnvironmentProcessorsFactory.class);
    private final ExtensionContext extensionContext
    private final ExecutableApiFacade facade
    private final RemoteEnvironmentPackageContext packageContext

    RemoteEnvironmentProcessorsFactory(ExecutableApiFacade facade, ExtensionContext extensionContext, RemoteEnvironmentPackageContext packageContext) {
        this.facade = facade
        this.extensionContext = extensionContext
        this.packageContext = packageContext
    }

    RemoteEnvironmentProcessor create(TypedScript script) {
        def executor = ScriptExecutorProvider.findExecutor(extensionContext, script).orElseThrow {
            new RuntimeException("Unable to find executor of type ${script.type} used in script ${script.scriptName}")
        }
        def ansibleScriptContext = new AnsibleScriptContext(packageContext.remoteUser,
                new File(packageContext.keysDirectory, "id_rsa"),
                packageContext.ansibleInventoryFile,
                packageContext.knownHostsFile)
        def terraformScriptContext = new TerraformScriptContext(new File(packageContext.settingsDirectory, "terraform.state"))
        return {
            String command, Map<String, String> envs ->
                Map<String, String> mutableEnvs = envs
                Map<String, Object> context = [
                        "result"      : new ExecutableResults(),
                        "log"         : LOGGER,
                        "context"     : packageContext,
                        "api"         : facade,
                        "dir"         : script.baseDirectory,
                        "currentNodes": facade.nodes,
                        "env"         : mutableEnvs,
                        "command"     : command,
                        "ansible"     : ansibleScriptContext,
                        "terraform"   : terraformScriptContext,
                        "processor"   : this,
                ]
                executor.execute(context)
                return context["result"] as ExecutableResults
        } as RemoteEnvironmentProcessor

    }

    @Override
    ExecutableResults run(String name, String command, String script, Map<String, String> env) {
        def processor = create(new RemoteEnvironmentScript(name, script, packageContext.gav, packageContext.packageRootDirectory))
        return processor.process(command, env)
    }
}
