package org.s4s0l.shathel.commons.remoteswarm

import org.s4s0l.shathel.commons.scripts.TypedScript

/**
 * @author Marcin Wielgus
 */
interface RemoteEnvironmentProcessor {

    Map<String, String> process(ProcessorCommand command, Map<String, String> envs)

}

enum ProcessorCommand {
    APPLY,
    START,
    STOP,
    DESTROY,
    STARTED
}
