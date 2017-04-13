package org.s4s0l.shathel.commons.secrets

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import org.s4s0l.shathel.commons.core.environment.EnricherExecutable
import org.s4s0l.shathel.commons.core.environment.EnricherExecutableParams
import org.s4s0l.shathel.commons.core.environment.ProvisionerExecutable
import org.s4s0l.shathel.commons.scripts.NamedExecutable

/**
 * @author Marcin Wielgus
 */
@TypeChecked
@CompileStatic
class SecretsEnricher extends EnricherExecutable {

    @Override
    protected void execute(EnricherExecutableParams params) {
        def apiFacade = params.getApiFacade()
        def model = params.model
        def stack = params.stack
        def manager = apiFacade.getSecretManager()
        def provisioners = params.getProvisioners()
        model.mapSecrets {
            Map<String,String> secret ->
                if (secret.name.startsWith("shathel_")) {
                    if (secret.file != null) {
                        //this means secret is defined here so we rule it
                        if (manager.secretExists(secret.name)) {
                            //it exists so we externalize it with current name
                            def currentName = manager.secretCurrentName(secret.name)
                            return [name: currentName, external: true]
                        } else {
                            //does not exist will be created in pre provisioning
                            provisioners.add("secret-add:${secret.name}", { context ->
                                manager.secretCreate(secret.name, new File(stack.getStackResources().getComposeFileDirectory(), secret.file))
                            } as ProvisionerExecutable)
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
    }
}

