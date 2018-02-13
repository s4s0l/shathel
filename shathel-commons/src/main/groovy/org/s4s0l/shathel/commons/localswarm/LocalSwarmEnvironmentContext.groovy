package org.s4s0l.shathel.commons.localswarm

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import org.s4s0l.shathel.commons.core.environment.EnvironmentContext
import org.s4s0l.shathel.commons.core.environment.EnvironmentContextInternal

/**
 * @author Marcin Wielgus
 */
@TypeChecked
@CompileStatic
interface LocalSwarmEnvironmentContext extends EnvironmentContext , EnvironmentContextInternal{

    String getRemoteUser()

    File getDataDirectory()

    Optional<String> getEnvironmentParameter(String name)

    Optional<Integer> getEnvironmentParameterAsInt(String name)

    Optional<Boolean> getEnvironmentParameterAsBoolean(String name)
}