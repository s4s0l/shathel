package org.s4s0l.shathel.commons.secrets

import org.s4s0l.shathel.commons.core.environment.EnricherExecutable
import org.s4s0l.shathel.commons.core.environment.EnvironmentContext
import org.s4s0l.shathel.commons.core.environment.ExecutableApiFacade
import org.s4s0l.shathel.commons.core.model.ComposeFileModel
import org.s4s0l.shathel.commons.core.stack.StackDescription
import org.s4s0l.shathel.commons.scripts.Executable

/**
 * @author Matcin Wielgus
 */
class SecretsEnricher extends EnricherExecutable {

    @Override
    protected List<Executable> executeProvidingProvisioner(EnvironmentContext environmentContext,
                                                           ExecutableApiFacade apiFacade,
                                                           StackDescription stack,
                                                           ComposeFileModel model) {
        def manager = apiFacade.getSecretManager()
        List<Executable> executables = []
        model.mapSecrets {
            secret ->
                if (secret.name.startsWith("shathel_")) {
                    if (secret.file != null) {
                        //this means secret is defined here so we rule it
                        if (manager.secretExists(secret.name)) {
                            //it exists so we externalize it with current name
                            def currentName = manager.secretCurrentName(secret.name);
                            return [name: currentName, external: true]
                        } else {
                            //does not exist will be created in pre provisioning
                            executables.add({ context ->
                                manager.secretCreate(secret.name, new File(stack.getStackResources().getComposeFileDirectory(), secret.file))
                                return "ok"
                            } as Executable)
                            return [name: manager.secretInitialName(secret.name), external: true]
                        }

                    } else if (secret.external) {
//                        it is external so just swap with current name

                        //we asume someone will create this secret
                        def currentName = manager.secretInitialName(secret.name)
                        if (manager.secretExists(secret.name)) {
                            //or it exists, so using what is
                            currentName = manager.secretCurrentName(secret.name)
                        }
                        return [name: currentName, external: true]
                    } else {
                        throw new RuntimeException("Sth wrong with secrets definition in stack ${stack.deployName}. Should have file or external set to true (${secret.name})")
                    }
                } else {
                    secret
                }

        }
        return executables
    }
}

