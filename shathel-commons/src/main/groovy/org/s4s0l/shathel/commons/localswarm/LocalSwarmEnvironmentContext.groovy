package org.s4s0l.shathel.commons.localswarm

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import org.s4s0l.shathel.commons.core.environment.EnvironmentContext

/**
 * @author Marcin Wielgus
 */
@TypeChecked
@CompileStatic
interface LocalSwarmEnvironmentContext extends EnvironmentContext {

    String getRemoteUser()

}