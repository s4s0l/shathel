package org.s4s0l.shathel.commons.remoteswarm

/**
 * @author Marcin Wielgus
 */
interface RemoteEnvironmentCallbackProcessors {

    Map<String,String> vagrant(String command, String script, Map<String,String> env)

    Map<String,String> ansible(String command, String script, Map<String,String> env)

}