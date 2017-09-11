package org.s4s0l.shathel.commons.secrets

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import org.s4s0l.shathel.commons.core.ParameterProvider
import org.s4s0l.shathel.commons.core.security.SimpleEncryptor
import org.s4s0l.shathel.commons.docker.DockerClientWrapper
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.util.function.Supplier

/**
 * @author Marcin Wielgus
 */

class SecretManager implements SecretManagerApi {
    private static
    final Logger LOGGER = LoggerFactory.getLogger(SecretManager.class)
    private final ParameterProvider parameters
    private final DockerClientWrapper dockerWrapper
    private final SimpleEncryptor encryptor


    SecretManager(ParameterProvider parameters, DockerClientWrapper dockerWrapper, SimpleEncryptor encryptor) {
        this.parameters = parameters
        this.dockerWrapper = dockerWrapper
        this.encryptor = encryptor
    }

    @TypeChecked
    @CompileStatic
    @Override
    boolean secretExists(String secretName) {
        return !getAllSecrets(secretName).isEmpty()
    }

    @Override
    String secretCurrentName(String secretName) {
        return getAllSecrets(secretName)[0].Spec.Name
    }

    @TypeChecked
    @CompileStatic
    @Override
    String secretInitialName(String secretName) {
        secretName + "_1"
    }

    @Override
    String secretCreate(String secretName, File defaultValue) {
        secretCreate(secretName, { defaultValue.bytes } as Supplier<byte[]>)
    }

    @Override
    String secretUpdate(String secretName, File defaultValue) {
        secretUpdate(secretName, { defaultValue.bytes } as Supplier<byte[]>)
    }
/**
 *
 * @param secretName name of secret
 * @param defaultValue file from which to get default value
 * @return current secret name
 */
    @TypeChecked
    @Override
    @CompileStatic
    String secretCreate(String secretName, Supplier<byte[]> defaultValue) {
        List secretsMatching = getAllSecrets(secretName)
        if (!secretsMatching.isEmpty()) {
            throw new RuntimeException("Secret $secretName already present")
        }
        def name = secretInitialName(secretName)
        def value = getValue(secretName, defaultValue)
        dockerWrapper.createSecret(name, value, ['shathel.password': 'true', 'shathel.password.version': '1'])
        return value
    }

    @TypeChecked
    @CompileStatic
    @Override
    String secretUpdate(String secretName, Supplier<byte[]> defaultValue) {
        def finalName = secretAddNewVersion(secretName, defaultValue)
        secretUpdateForServices(secretName)
        return finalName
    }


    private String secretAddNewVersion(String secretName, Supplier<byte[]> defaultValue) {
        List secretsMatching = getAllSecrets(secretName)
        if (secretsMatching.isEmpty()) {
            LOGGER.warn("Secret does not exist so creating new")
            secretCreate(secretName, defaultValue)
        }
        int secretNum = 0
        def x = secretsMatching[0].Spec.Name =~ /.*_([0-9]+)$/
        if (x.matches()) {
            secretNum = Integer.parseInt(x[0][1])
        }
        secretNum = secretNum + 1
        String finalSecretName = "${secretName}_$secretNum"
        def value = getValue(secretName, defaultValue)
        dockerWrapper.createSecret(finalSecretName, value, ['shathel.password': 'true', 'shathel.password.version': "$secretNum"])

        return finalSecretName
    }

    @Override
    List<String> getAllSecretNames() {
        List secretsMatching = dockerWrapper.secrets().content
        return secretsMatching.sort {
            o1, o2 -> -o1.Spec.Name.compareTo(o2.Spec.Name)
        }.collect { it.Spec.Name }
    }

    @Override
    List<String> getAllSecretNames(String secretName) {
        def pattern = /${secretName}(_[0-9]+)?/
        List secretsList = dockerWrapper.secrets().content
        def secretsMatching = secretsList.findAll {
            it.Spec.Name.startsWith(secretName) && it.Spec.Name ==~ pattern
        }
        return secretsMatching.sort {
            o1, o2 -> -o1.Spec.Name.compareTo(o2.Spec.Name)
        }.collect { it.Spec.Name }
    }

    @Override
    List<String> getServicesUsingSecret(String secretName) {
        List secretsMatching = getAllSecrets(secretName)
        if (secretsMatching.isEmpty()) {
            throw new RuntimeException("Secret $secretName is not present")
        }
        List<String> oldPasswordsID = secretsMatching.collect { it.ID }
        def servicesToReload = dockerWrapper.services().content
                .findAll {
            it?.Spec?.TaskTemplate?.ContainerSpec?.Secrets?.find { s ->
                oldPasswordsID.contains(s.SecretID)
            } != null

        }
        return servicesToReload.collect {
            it.Spec.Name
        }.sort()
    }

    private void secretUpdateForServices(String secretName) {
        List secretsMatching = getAllSecrets(secretName)
        if (secretsMatching.isEmpty()) {
            throw new RuntimeException("Secret $secretName is not present")
        }
        if (secretsMatching.size() == 1) {
            LOGGER.warn("Secret $secretName has only one version so not updating.")
        }
        List<String> oldPasswordsID = secretsMatching.tail().collect { it.ID }

        def servicesToReload = dockerWrapper.services().content
                .findAll {
            it?.Spec?.TaskTemplate?.ContainerSpec?.Secrets?.find { s ->
                oldPasswordsID.contains(s.SecretID)
            } != null

        }



        String finalSecretName = secretsMatching[0].Spec.Name
        String finalSecretID = secretsMatching[0].ID

        servicesToReload.each {
            it.Spec.TaskTemplate?.ContainerSpec?.Secrets.each { s ->
                if (oldPasswordsID.contains(s.SecretID)) {
                    s.SecretID = finalSecretID
                    s.SecretName = finalSecretName
                }

            }
            try {
                LOGGER.info("Updating service ${it.Spec.Name} with new secrets")
                dockerWrapper.updateService(it.ID, [version: it.Version.Index], it.Spec)
            } catch (Exception e) {
                throw new RuntimeException("Unable to swap password for service ${it.Spec.Name}")
            }
            LOGGER.info("Waiting for update process to finish for service ${it.Spec.Name}")
            def status = "updating"
            while (status != "completed") {
                status = dockerWrapper.inspectService(it.ID).content?.UpdateStatus?.State
                LOGGER.debug("Waiting for update status completed for service ${it.Spec.Name}. Status is ${status}...")
                if (status != "completed") {
                    Thread.sleep(1000)
                }
            }
        }
    }


    private byte[] getValue(String secretName, Supplier<byte[]> defaultValue) {
        return parameters.getParameter(secretName.toLowerCase() + "_secret_path")
                .map { new File(it).bytes }
                .orElseGet {
            parameters.getParameter(secretName.toLowerCase() + "_secret_value")
                    .map { encryptor.decrypt(it) }
                    .map { it.bytes }
                    .orElseGet {
                if (defaultValue == null) {
                    throw new RuntimeException("Unable to find value of secret $secretName.")
                }
                LOGGER.warn("For secret $secretName default value will be used from supplier.")
                defaultValue.get()
            }
        }
    }

    private List getAllSecrets(String secretName) {
        def pattern = /${secretName}(_[0-9]+)?/
        List secretsList = dockerWrapper.secrets().content
        def secretsMatching = secretsList.findAll {
            it.Spec.Name.startsWith(secretName) && it.Spec.Name ==~ pattern
        }
        secretsMatching.sort {
            o1, o2 -> -o1.Spec.Name.compareTo(o2.Spec.Name)
        }
    }
}
