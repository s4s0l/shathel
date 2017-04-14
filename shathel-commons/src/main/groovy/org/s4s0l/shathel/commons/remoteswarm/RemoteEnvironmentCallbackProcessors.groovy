package org.s4s0l.shathel.commons.remoteswarm

import org.s4s0l.shathel.commons.scripts.ExecutableResults

/**
 * @author Marcin Wielgus
 */
interface RemoteEnvironmentCallbackProcessors {


    ExecutableResults run(String name, String command, String script, Map<String, String> env)

}