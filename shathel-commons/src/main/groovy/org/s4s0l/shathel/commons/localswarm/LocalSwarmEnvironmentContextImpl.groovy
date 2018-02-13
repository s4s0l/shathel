package org.s4s0l.shathel.commons.localswarm

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import org.s4s0l.shathel.commons.core.environment.EnvironmentContext
import org.s4s0l.shathel.commons.core.environment.EnvironmentDescription
import org.s4s0l.shathel.commons.utils.Utils

/**
 * @author Marcin Wielgus
 */
@TypeChecked
@CompileStatic
class LocalSwarmEnvironmentContextImpl implements LocalSwarmEnvironmentContext {

    @Delegate
    private final EnvironmentContext environmentContext
    private final EnvironmentDescription environmentDescription


    LocalSwarmEnvironmentContextImpl(EnvironmentDescription environmentDescription, EnvironmentContext environmentContext) {
        this.environmentContext = environmentContext
        this.environmentDescription = environmentDescription
    }

    public Optional<String> getEnvironmentParameter(String name) {
        return environmentDescription.getEnvironmentParameter(name)
    }

    public Optional<Integer> getEnvironmentParameterAsInt(String name) {
        return environmentDescription.getEnvironmentParameterAsInt(name)
    }

    public Optional<Boolean> getEnvironmentParameterAsBoolean(String name) {
        return environmentDescription.getEnvironmentParameterAsBoolean(name)
    }

    @Override
    String getRemoteUser() {
        return System.getProperty("user.name")
    }

    @Override
    Map<String, String> getAsEnvironmentVariables() {
        Map<String, String> ret = new HashMap<>()
        ret.putAll(environmentContext.asEnvironmentVariables)
        addCalculatedEnvironmentVariables(ret)
        ret.putAll([
                "SHATHEL_ENVPACKAGE_VERSION"          : Utils.getShathelVersion(),
                "SHATHEL_ENVPACKAGE_SETTINGS_DIR"     : settingsDirectory.absolutePath,
                "SHATHEL_ENVPACKAGE_TMP_DIR"          : tempDirectory.absolutePath,
                "SHATHEL_ENVPACKAGE_ANSIBLE_INVENTORY": ansibleInventoryFile.absolutePath,
                "SHATHEL_ENVPACKAGE_USER"             : remoteUser,
                "SHATHEL_ENVPACKAGE_CERTS_DIR"        : certsDirectory.absolutePath,
        ])
        return ret
    }

    private void addCalculatedEnvironmentVariables(Map<String, String> ret) {

        ret.put("SHATHEL_ENV_SIZE", "" + 1)
        ret.put("SHATHEL_ENV_QUORUM", "" + 1)
        ret.put("SHATHEL_ENV_MGM_SIZE", "" + 1)
        ret.put("SHATHEL_ENV_MGM_QUORUM", "" + 1)
        ret.put("SHATHEL_ENV_DOMAIN", environmentDescription.getEnvironmentParameter("domain")
                .orElse("localhost"))
    }

}
