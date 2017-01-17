package org.s4s0l.shathel.commons.core;

import java.io.File;

/**
 * @author Matcin Wielgus
 */
public interface EnvironmentContainerRunnerContext extends AutoCloseable{
    void runContainers(StackDescription description, File composeFile);
}
