package org.s4s0l.shathel.commons.localswarm

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import org.s4s0l.shathel.commons.core.environment.EnvironmentContext
import org.s4s0l.shathel.commons.utils.Utils

/**
 * @author Marcin Wielgus
 */
@TypeChecked
@CompileStatic
class LocalSwarmEnvironmentContextImpl implements LocalSwarmEnvironmentContext {

    @Delegate
    private final EnvironmentContext environmentContext;


    LocalSwarmEnvironmentContextImpl(EnvironmentContext environmentContext) {
        this.environmentContext = environmentContext
    }

    @Override
    String getRemoteUser() {
        return System.getProperty("user.name")
    }

    @Override
    Map<String, String> getAsEnvironmentVariables() {
        Map<String, String> ret = new HashMap<>()
        ret.putAll(environmentContext.asEnvironmentVariables)
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

}
