package org.s4s0l.shathel.commons.remoteswarm

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import org.s4s0l.shathel.commons.core.environment.EnvironmentContext

/**
 * @author Marcin Wielgus
 */

@TypeChecked
@CompileStatic
interface RemoteEnvironmentPackageContext extends EnvironmentContext {
    String getRemoteUser()

    RemoteEnvironmentPackageDescription getDescription()

    File getKeysDirectory()

    File getCertsDirectory()

    File getAnsibleInventoryFile()

    File getKnownHostsFile()

    File getPackageRootDirectory()

    String getGav()
}