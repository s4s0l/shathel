package org.s4s0l.shathel.commons.core.environment;

import org.s4s0l.shathel.commons.core.stack.StackDescription;

import java.io.File;
import java.util.Map;

/**
 * @author Marcin Wielgus
 */
public interface EnvironmentContainerRunnerContext extends AutoCloseable{
    void startContainers(String deployName, Map<String,String> environment, File composeFile);

    void stopContainers(String deployName, Map<String,String> environment, File composeFile);
}
