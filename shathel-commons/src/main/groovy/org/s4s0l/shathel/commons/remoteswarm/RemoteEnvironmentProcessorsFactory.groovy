package org.s4s0l.shathel.commons.remoteswarm

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import org.s4s0l.shathel.commons.core.environment.ExecutableApiFacade
import org.s4s0l.shathel.commons.scripts.ScriptExecutorProvider
import org.s4s0l.shathel.commons.scripts.TypedScript
import org.s4s0l.shathel.commons.scripts.ansible.AnsibleScriptContext
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * @author Marcin Wielgus
 */
@TypeChecked
@CompileStatic
class RemoteEnvironmentProcessorsFactory {
    private static
    final Logger LOGGER = LoggerFactory.getLogger(RemoteEnvironmentProcessorsFactory.class);
    ExecutableApiFacade facade;


    RemoteEnvironmentProcessor create(RemoteEnvironmentPackageContext packageContext, TypedScript script) {
        def executor = ScriptExecutorProvider.findExecutor(packageContext.extensionContext, script).get()
        def ansibleScriptContext = new AnsibleScriptContext(packageContext.packageDescription.remoteUser,
                new File(packageContext.keysDirectory, "id_rsa"),
                packageContext.ansibleInventoryFile)

        return {
            ProcessorCommand command, Map<String, String> envs ->
                Map<String, String> mutableEnvs = new HashMap<>(envs)
                Map<String, Object> context = [
                        "log"         : LOGGER,
                        "context"     : packageContext.environmentContext,
                        "api"         : facade,
                        "dir"         : script.baseDirectory,
                        "currentNodes": facade.nodes,
                        "env"         : mutableEnvs,
                        "command"     : command.toString(),
                        "ansible"     : ansibleScriptContext
                ]
                executor.execute(context)
                return mutableEnvs
        } as RemoteEnvironmentProcessor

    }

}
