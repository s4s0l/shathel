package org.s4s0l.shathel.commons.remoteswarm

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import org.s4s0l.shathel.commons.core.environment.EnvironmentContext


/**
 * @author Marcin Wielgus
 */
@TypeChecked
@CompileStatic
class RemoteEnvironmentPackageContextImpl implements RemoteEnvironmentPackageContext {

    @Delegate
    private final EnvironmentContext environmentContext
    private final RemoteEnvironmentPackageDescription description

    RemoteEnvironmentPackageContextImpl(EnvironmentContext environmentContext, RemoteEnvironmentPackageDescription description) {
        this.environmentContext = environmentContext
        this.description = description
    }

    EnvironmentContext getEnvironmentContext() {
        return environmentContext
    }

    @Override
    String getRemoteUser() {
        return description.remoteUser
    }

    @Override
    RemoteEnvironmentPackageDescription getDescription() {
        return description
    }

    @Override
    File getKeysDirectory() {
        return ensureExists(new File(settingsDirectory, "keys"))
    }

    @Override
    File getCertsDirectory() {
        return ensureExists(new File(settingsDirectory, "certs"))
    }

    @Override
    File getAnsibleInventoryFile() {
        return new File(settingsDirectory, "vagrant-inventory")
    }

    @Override
    File getKnownHostsFile() {
        return new File(settingsDirectory, "known-hosts")
    }

    @Override
    File getPackageRootDirectory() {
        return description.packageRootDirectory
    }

    @Override
    String getGav() {
        return description.gav
    }

    @Override
    Map<String, String> getAsEnvironmentVariables() {
        Map<String, String> ret = [
                "SHATHEL_ENVPACKAGE_VERSION"          : description.version,
                "SHATHEL_ENVPACKAGE_SETTINGS_DIR"     : settingsDirectory.absolutePath,
                "SHATHEL_ENVPACKAGE_IMAGE_NAME"       : description.envPackageImage,
                "SHATHEL_ENVPACKAGE_KEY_DIR"          : keysDirectory.absolutePath,
                "SHATHEL_ENVPACKAGE_TMP_DIR"          : tempDirectory.absolutePath,
                "SHATHEL_ENVPACKAGE_WORKING_DIR"      : description.packageRootDirectory.absolutePath,
                "SHATHEL_ENVPACKAGE_ANSIBLE_INVENTORY": ansibleInventoryFile.absolutePath,
                "SHATHEL_ENVPACKAGE_USER"             : description.remoteUser,
                "SHATHEL_ENVPACKAGE_CERTS_DIR"        : certsDirectory.absolutePath,
        ]
        ret.putAll(environmentContext.asEnvironmentVariables)
        return ret
    }

    private File ensureExists(File f) {
        f.mkdirs()
        return f
    }
}
