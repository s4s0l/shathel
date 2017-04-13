package org.s4s0l.shathel.commons.remoteswarm

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import org.s4s0l.shathel.commons.core.environment.EnvironmentContext

/**
 * @author Marcin Wielgus
 */
@TypeChecked
@CompileStatic
class RemoteEnvironmentPackageContext {

    @Delegate
    private final EnvironmentContext environmentContext
    private final RemoteEnvironmentPackageDescription description

    RemoteEnvironmentPackageContext(EnvironmentContext environmentContext, RemoteEnvironmentPackageDescription description) {
        this.environmentContext = environmentContext
        this.description = description
    }

    EnvironmentContext getEnvironmentContext() {
        return environmentContext
    }

    RemoteEnvironmentPackageDescription getPackageDescription() {
        return description
    }

    String getEnvPackageVersion() {
        return description.version
    }

    String getEnvPackageImage() {
        return "shathel-env-${envPackageVersion}"
    }

    private File ensureExists(File f) {
        f.mkdirs()
        return f
    }

    File getKeysDirectory() {
        return ensureExists(new File(settingsDirectory, "keys"))
    }

    File getCertsDirectory() {
        return ensureExists(new File(settingsDirectory, "certs"))
    }

    File getAnsibleInventoryFile() {
        return new File(settingsDirectory, "vagrant-inventory")
    }

    File getKnownHostsFile() {
        return new File(settingsDirectory, "known-hosts")
    }

    File getPackageRootDirectory() {
        return description.packageRootDirectory
    }

    Map<String, String> getProcessorVariables() {
        Map<String, String> ret = [
                "SHATHEL_ENVPACKAGE_VERSION"          : envPackageVersion,
                "SHATHEL_ENVPACKAGE_SETTINGS_DIR"     : settingsDirectory.absolutePath,
                "SHATHEL_ENVPACKAGE_IMAGE_NAME"       : envPackageImage,
                "SHATHEL_ENVPACKAGE_KEY_DIR"          : keysDirectory.absolutePath,
                "SHATHEL_ENVPACKAGE_TMP_DIR"          : tempDirectory.absolutePath,
                "SHATHEL_ENVPACKAGE_WORKING_DIR"      : packageRootDirectory.absolutePath,
                "SHATHEL_ENVPACKAGE_ANSIBLE_INVENTORY": ansibleInventoryFile.absolutePath,
                "SHATHEL_ENVPACKAGE_USER"             : description.remoteUser,
                "SHATHEL_ENVPACKAGE_CERTS_DIR"        : certsDirectory.absolutePath,
        ]
        ret.putAll(environmentContext.asEnvironmentVariables)
        return ret
    }
}
