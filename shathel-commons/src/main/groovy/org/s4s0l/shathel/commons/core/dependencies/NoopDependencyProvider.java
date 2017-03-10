package org.s4s0l.shathel.commons.core.dependencies;

import org.s4s0l.shathel.commons.core.stack.StackReference;

import java.io.File;

/**
 * @author Marcin Wielgus
 */
public class NoopDependencyProvider implements DependencyDownloader {
    @Override
    public void download(StackReference reference, File directory) {
        throw new UnsupportedOperationException("Dependencies should be in directory!!");
    }
}
