package org.s4s0l.shathel.commons.localcompose;

import org.s4s0l.shathel.commons.core.environment.*;
import org.s4s0l.shathel.commons.docker.DockerComposeWrapper;
import org.s4s0l.shathel.commons.docker.DockerWrapper;
import org.s4s0l.shathel.commons.scripts.Executable;
import org.slf4j.Logger;

import java.util.Arrays;
import java.util.List;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * @author Marcin Wielgus
 */
public class LocalEnvironment implements Environment {

    private final EnvironmentContext context;

    public LocalEnvironment(EnvironmentContext context) {
        this.context = context;
    }


    @Override
    public boolean isInitialized() {
        return true;
    }

    @Override
    public void initialize() {

    }

    @Override
    public void save() {

    }

    @Override
    public void load() {

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
        new DockerComposeWrapper().version();
    }

    @Override
    public StackIntrospectionProvider getIntrospectionProvider() {
        return new LocalStackIntrospectionProvider();
    }


    @Override
    public EnvironmentContainerRunner getContainerRunner() {
        return new LocalEnvironmentContainerRunner();
    }

    @Override
    public EnvironmentContext getEnvironmentContext() {
        return context;
    }

    @Override
    public List<Executable> getEnvironmentEnrichers() {
        return Arrays.asList(new LocalMountingEnricher(),
                new MandatoryEnvironmentsValidator());
    }

    private static final Logger LOGGER = getLogger(LocalEnvironment.class);

    @Override
    public ExecutableApiFacade getEnvironmentApiFacade() {
        return new LocalExecutableApiFacade(new DockerWrapper());
    }

}
