package org.s4s0l.shathel.commons.localcompose;

import org.s4s0l.shathel.commons.core.environment.Environment;
import org.s4s0l.shathel.commons.core.environment.EnvironmentContainerRunner;
import org.s4s0l.shathel.commons.core.environment.StackIntrospectionProvider;
import org.s4s0l.shathel.commons.core.provision.EnvironmentProvisionExecutor;
import org.s4s0l.shathel.commons.core.security.SafeStorage;
import org.s4s0l.shathel.commons.core.storage.Storage;

import java.io.File;

/**
 * @author Matcin Wielgus
 */
public class LocalEnvironment implements Environment {

    private final File temporaryDir;
    private final File workDir;


    public LocalEnvironment(File temporaryDir, File workDir) {
        this.temporaryDir = temporaryDir;
        this.workDir = workDir;
    }


    @Override
    public File getExecutionDirectory() {
        File execution = new File(temporaryDir, "execution");
        execution.mkdirs();
        return execution;
    }

    @Override
    public boolean isInitialized() {
        return true;
    }

    @Override
    public void initialize() {

    }

    @Override
    public void start() {

    }

    @Override
    public boolean isStarted() {
        return true;
    }

    @Override
    public void stop() {

    }

    @Override
    public void destroy() {
        //todo zatrzymac wszystkie kontenery z composow zawierajacych nasze kontenery
        //wszystkie sieci z labelka (jak wywalic sieci domyslne?
        //volumeny z laabelka
//        itd
    }

    @Override
    public void verify() {

    }

    @Override
    public StackIntrospectionProvider getIntrospectionProvider() {
        return new LocalStackIntrospectionProvider();
    }

    @Override
    public EnvironmentProvisionExecutor getProvisionExecutor() {
        File execution = new File(workDir, "mounts");
        execution.mkdirs();
        return new LocalEnvironmentProvisionExecutor(execution);
    }

    @Override
    public EnvironmentContainerRunner getContainerRunner() {
        return new LocalEnvironmentContainerRunner();
    }

}
