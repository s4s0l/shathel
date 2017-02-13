package org.s4s0l.shathel.commons.localcompose;

import org.s4s0l.shathel.commons.core.environment.*;
import org.s4s0l.shathel.commons.core.provision.DefaultProvisionerExecutor;
import org.s4s0l.shathel.commons.core.provision.EnvironmentProvisionExecutor;
import org.s4s0l.shathel.commons.docker.DockerComposeWrapper;
import org.s4s0l.shathel.commons.docker.DockerWrapper;
import org.s4s0l.shathel.commons.scripts.Executor;
import org.s4s0l.shathel.commons.utils.ExtensionContext;
import org.slf4j.Logger;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * @author Matcin Wielgus
 */
public class LocalEnvironment implements Environment {

    private final EnvironmentContext context;

    public LocalEnvironment(EnvironmentContext context) {
        this.context = context;
    }


    @Override
    public File getExecutionDirectory() {
        return context.getExecutionDirectory();
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
    public EnvironmentProvisionExecutor getProvisionExecutor() {
        return new DefaultProvisionerExecutor(this);
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
    public List<Executor> getEnvironmentEnrichers() {
        return Arrays.asList(new LocalMountingEnricher());
    }

    private static final Logger LOGGER = getLogger(LocalEnvironment.class);

    @Override
    public EnvironmentApiFacade getEnvironmentApiFacade() {
        return new EnvironmentApiFacade() {
            @Override
            public List<String> getNodeNames() {
                return Collections.singletonList("localhost");
            }

            @Override
            public String getIp(String nodeName) {
                return "localhost";
            }

            @Override
            public String getIpForManagementNode() {
                return "localhost";
            }

            @Override
            public DockerWrapper getDockerForManagementNode() {
                return new DockerWrapper();
            }

            @Override
            public DockerWrapper getDocker(String nodeName) {
                if ("localhost".equals(nodeName)) {
                    return getDockerForManagementNode();
                } else {
                    throw new RuntimeException("Unknown node name " + nodeName);
                }
            }


            @Override
            public void setKernelParam(String param) {
                LOGGER.warn("!Set parameter like: sudo sysctl -w " + param);
            }

            @Override
            public Map<String, String> getDockerEnvs(String nodeName) {
                return Collections.emptyMap();
            }

            @Override
            public int getExpectedNodeCount() {
                return 1;
            }

            @Override
            public int getExpectedManagerNodeCount() {
                return 1;
            }
        };
    }

}
