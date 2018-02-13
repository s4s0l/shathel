package org.s4s0l.shathel.commons.remoteswarm

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import org.s4s0l.shathel.commons.core.environment.EnvironmentContext
import org.s4s0l.shathel.commons.core.environment.EnvironmentDescription


/**
 * @author Marcin Wielgus
 */
@TypeChecked
@CompileStatic
class RemoteEnvironmentPackageContextImpl implements RemoteEnvironmentPackageContext {

    @Delegate
    private final EnvironmentContext environmentContext
    @Delegate
    private final RemoteEnvironmentPackageDescription description
    private final EnvironmentDescription environmentDescription

    RemoteEnvironmentPackageContextImpl(EnvironmentContext environmentContext,
                                        EnvironmentDescription environmentDescription,
                                        RemoteEnvironmentPackageDescription description) {
        this.environmentContext = environmentContext
        this.description = description
        this.environmentDescription = environmentDescription
    }

    Optional<String> getEnvironmentParameter(String name) {
        return environmentDescription.getEnvironmentParameter(name)
    }

    Optional<Integer> getEnvironmentParameterAsInt(String name) {
        return environmentDescription.getEnvironmentParameterAsInt(name)
    }

    Optional<Boolean> getEnvironmentParameterAsBoolean(String name) {
        return environmentDescription.getEnvironmentParameterAsBoolean(name)
    }


    EnvironmentContext getEnvironmentContext() {
        return environmentContext
    }

    @Override
    String getRemoteUser() {
        return description.remoteUser
    }



    @Override
    File getKeysDirectory() {
        return ensureExists(new File(settingsDirectory, "keys"))
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
    String getEmail() {
        return environmentDescription.getEnvironmentParameter("email").orElse("someone@${contextName}".toString())
    }

    @Override
    Map<String, String> getAsEnvironmentVariables() {
        Map<String, String> ret = new HashMap<>()
        ret.putAll(environmentContext.asEnvironmentVariables)
        addCalculatedEnvironmentVariables(ret)
        ret.putAll([
                "SHATHEL_ENVPACKAGE_VERSION"          : description.version,
                "SHATHEL_ENVPACKAGE_SETTINGS_DIR"     : settingsDirectory.absolutePath,
                "SHATHEL_ENVPACKAGE_IMAGE_NAME"       : description.envPackageImage,
                "SHATHEL_ENVPACKAGE_KEY_DIR"          : keysDirectory.absolutePath,
                "SHATHEL_ENVPACKAGE_TMP_DIR"          : tempDirectory.absolutePath,
                "SHATHEL_ENVPACKAGE_WORKING_DIR"      : description.packageRootDirectory.absolutePath,
                "SHATHEL_ENVPACKAGE_ANSIBLE_INVENTORY": ansibleInventoryFile.absolutePath,
                "SHATHEL_ENVPACKAGE_USER"             : description.remoteUser,
                "SHATHEL_ENVPACKAGE_CERTS_DIR"        : certsDirectory.absolutePath,
        ])
        return ret
    }

    private static File ensureExists(File f) {
        f.mkdirs()
        return f
    }


    int getNodesCount() {
        return getManagersCount() + getWorkersCount()
    }

    int getManagersCount() {
        return environmentDescription.getEnvironmentParameterAsInt("managers")
                .orElse(1)
    }

    int getWorkersCount() {
        return environmentDescription.getEnvironmentParameterAsInt("workers")
                .orElse(0)
    }

    private void addCalculatedEnvironmentVariables(Map<String, String> ret) {
        int size = getNodesCount()
        int quorum = (int) Math.floor((size / 2).toDouble()) + 1

        ret.put("SHATHEL_ENV_SIZE", "" + size)
        ret.put("SHATHEL_ENV_QUORUM", "" + quorum)

        int msize = getManagersCount()
        int mquorum = (int) Math.floor((msize / 2).toDouble()) + 1

        ret.put("SHATHEL_ENV_MGM_SIZE", "" + msize)
        ret.put("SHATHEL_ENV_MGM_QUORUM", "" + mquorum)
        ret.put("SHATHEL_ENV_DOMAIN", environmentDescription.getEnvironmentParameter("domain")
                .orElse("localhost"))
    }
}
