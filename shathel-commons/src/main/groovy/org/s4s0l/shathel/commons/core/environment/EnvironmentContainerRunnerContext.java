package org.s4s0l.shathel.commons.core.environment;

import org.s4s0l.shathel.commons.core.stack.StackDescription;

import java.io.File;

/**
 * @author Matcin Wielgus
 */
public interface EnvironmentContainerRunnerContext extends AutoCloseable{
    void startContainers(StackDescription description, File composeFile);

    void stopContainers(StackDescription description, File composeFile);
}
