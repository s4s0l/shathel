package org.s4s0l.shathel.commons.remoteswarm;

import java.util.Map;

/**
 * @author Marcin Wielgus
 */
public interface RemoteEnvironmentProcessor {

    default Map<String, String> process(ProcessorCommand command, Map<String, String> envs) {
        return process(command.toString(), envs);
    }

    Map<String, String> process(String command, Map<String, String> envs);

}

