package org.s4s0l.shathel.commons.remoteswarm;

import org.s4s0l.shathel.commons.scripts.ExecutableResults;

import java.util.Map;

/**
 * @author Marcin Wielgus
 */
public interface RemoteEnvironmentProcessor {

    default ExecutableResults process(ProcessorCommand command, Map<String, String> envs) {
        return process(command.toString(), envs);
    }

    ExecutableResults process(String command, Map<String, String> envs);

}

