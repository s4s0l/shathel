package org.s4s0l.shathel.commons.core;

import java.io.File;

/**
 * @author Matcin Wielgus
 */
public interface EnvironmentContainerRunner {


    EnvironmentContainerRunnerContext createContext(File executionDir);
}
