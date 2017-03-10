package org.s4s0l.shathel.commons.core.environment;

import org.s4s0l.shathel.commons.core.stack.StackDescription;

import java.io.File;

/**
 * @author Marcin Wielgus
 */
public interface EnvironmentContainerBuilder {
    void build(StackDescription description, File composeFile);
}
