package org.s4s0l.shathel.commons.core.environment;

import org.s4s0l.shathel.commons.core.stack.StackDescription;
import org.s4s0l.shathel.commons.core.stack.StackReference;

import java.io.File;
import java.util.Map;

/**
 * @author Marcin Wielgus
 */
public interface EnvironmentContainerRunnerContext extends AutoCloseable{
    void startContainers(StackCommand command, File composeFile);

    void stopContainers(StackCommand command,File composeFile);
}
