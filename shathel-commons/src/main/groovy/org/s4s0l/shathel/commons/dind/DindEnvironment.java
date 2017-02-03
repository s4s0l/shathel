package org.s4s0l.shathel.commons.dind;

import org.s4s0l.shathel.commons.core.environment.Environment;
import org.s4s0l.shathel.commons.core.environment.EnvironmentContainerRunner;
import org.s4s0l.shathel.commons.core.environment.StackIntrospectionProvider;
import org.s4s0l.shathel.commons.core.provision.EnvironmentProvisionExecutor;

import java.io.File;

/**
 * @author Matcin Wielgus
 */
public class DindEnvironment implements Environment {
    private final File temporaryDir;

    public DindEnvironment(File temporaryDir) {
        this.temporaryDir = temporaryDir;
    }

    @Override
    public File getExecutionDirectory() {
        File execution = new File(temporaryDir, "execution");
        execution.mkdirs();
        return execution;
    }

    @Override
    public boolean isInitialized() {
        return false;
    }

    @Override
    public void initialize() {

    }

    @Override
    public void start() {

    }

    @Override
    public boolean isStarted() {
        return false;
    }

    @Override
    public void stop() {

    }

    @Override
    public void destroy() {

    }

    @Override
    public void verify() {

    }

    @Override
    public void save() {

    }

    @Override
    public void load() {

    }

    @Override
    public StackIntrospectionProvider getIntrospectionProvider() {
        return null;
    }

    @Override
    public EnvironmentProvisionExecutor getProvisionExecutor() {
        return null;
    }

    @Override
    public EnvironmentContainerRunner getContainerRunner() {
        return null;
    }
}
