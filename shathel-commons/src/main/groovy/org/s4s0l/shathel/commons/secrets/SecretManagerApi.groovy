package org.s4s0l.shathel.commons.secrets

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked

import java.util.function.Supplier

/**
 * @author Marcin Wielgus
 */
@TypeChecked
@CompileStatic
interface SecretManagerApi {
    boolean secretExists(String secretName)

    String secretCurrentName(String secretName)

    String secretInitialName(String secretName)

    String secretCreate(String secretName, File defaultValue)

    String secretUpdate(String secretName, File defaultValue)

    String secretCreate(String secretName, Supplier<byte[]> defaultValue)

    String secretUpdate(String secretName, Supplier<byte[]> defaultValue)

    List<String> getAllSecretNames()

    List<String> getAllSecretNames(String secretName)

    List<String> getServicesUsingSecret(String secretName)
}
