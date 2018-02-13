package org.s4s0l.shathel.commons.remoteswarm

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import org.s4s0l.shathel.commons.core.environment.EnvironmentContext
import org.s4s0l.shathel.commons.core.environment.EnvironmentContextInternal
import org.s4s0l.shathel.commons.core.environment.EnvironmentDescription

/**
 * @author Marcin Wielgus
 */

@TypeChecked
@CompileStatic
interface RemoteEnvironmentPackageContext extends EnvironmentContext,EnvironmentContextInternal {

    Optional<String> getEnvironmentParameter(String name)

    Optional<Integer> getEnvironmentParameterAsInt(String name)

    Optional<Boolean> getEnvironmentParameterAsBoolean(String name)

    File getKeysDirectory()

    File getKnownHostsFile()

    File getPackageRootDirectory()

    String getRemoteUser()

    String getGav()

    String getEmail()

    int getNodesCount()

    int getManagersCount()

    int getWorkersCount()

    RemoteEnvironmentScript getImagePreparationScript()

    RemoteEnvironmentScript getInfrastructureScript()

    RemoteEnvironmentScript getSetupScript()

    RemoteEnvironmentScript getSwarmScript()

    String getEnvPackageImage()

    Map<String, String> getMandatoryEnvs()


}