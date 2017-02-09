package org.s4s0l.shathel.commons.swarm;

import groovy.lang.Tuple;
import org.s4s0l.shathel.commons.core.SettingsImporterExporter;
import org.s4s0l.shathel.commons.core.environment.*;
import org.s4s0l.shathel.commons.core.provision.DefaultProvisionerExecutor;
import org.s4s0l.shathel.commons.core.provision.EnvironmentProvisionExecutor;
import org.s4s0l.shathel.commons.docker.DockerInfoWrapper;
import org.s4s0l.shathel.commons.utils.ExtensionContext;
import org.slf4j.Logger;

import java.io.File;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * @author Matcin Wielgus
 */
public class SwarmEnvironment implements Environment {
    private static final Logger LOGGER = getLogger(SwarmEnvironment.class);
    private final EnvironmentContext environmentContext;
    private final SettingsImporterExporter machineSettingsImporterExporter;
    private final SwarmClusterWrapper swarmClusterWrapper;


    public SwarmEnvironment(EnvironmentContext environmentContext,
                            SettingsImporterExporter machineSettingsImporterExporter,
                            SwarmClusterWrapper swarmClusterWrapper, NodeProvisioner nodeProvisioner) {
        this.environmentContext = environmentContext;
        this.machineSettingsImporterExporter = machineSettingsImporterExporter;
        this.swarmClusterWrapper = swarmClusterWrapper;
        this.nodeProvisioner = nodeProvisioner;
    }

    private final NodeProvisioner nodeProvisioner;


    @Override
    public File getExecutionDirectory() {
        return environmentContext.getExecutionDirectory();
    }


    @Override
    public boolean isInitialized() {
        return swarmClusterWrapper.isInitialized(getManagersCount(), getWorkersCount());
    }


    @Override
    public void save() {
        String safeStoreKey = getSafeStorageKey();
        getImporterExporter().saveSettings(getDockerMachineStorageDir(),
                environmentContext.getSafeStorage().outputStream(safeStoreKey));
    }

    @Override
    public void load() {
        stop();
        String safeStoreKey = getSafeStorageKey();
        Optional<InputStream> inputStream = environmentContext.getSafeStorage().inputStream(safeStoreKey);
        if (inputStream.isPresent()) {
            if (isStarted()) {
                stop();
            }
            getImporterExporter().loadSettings(inputStream.get(),
                    getDockerMachineStorageDir());
        } else {
            throw new RuntimeException("No saved state found!");
        }
    }

    @Override
    public void initialize() {
        start();
        try {
            boolean changed = getNodeProvisioner().createMachines(
                    getDockerMachineStorageDir(),
                    environmentContext);

            if (changed) {
                LOGGER.info("Remember to save docker machine settings");
            }
        } catch (Exception e) {
            throw new RuntimeException("Initialization failed", e);
        }
    }


    private String getSafeStorageKey() {
        return "machines";
    }

    @Override
    public void start() {
        swarmClusterWrapper.getNodeNames().stream().forEach(swarmClusterWrapper::start);
    }

    @Override
    public boolean isStarted() {
        List<String> nodeNames = swarmClusterWrapper.getNodeNames();
        return !nodeNames.isEmpty() && !nodeNames.stream()
                .map(swarmClusterWrapper::getNode)
                .filter(x -> !x.isStarted())
                .findFirst()
                .isPresent();
    }

    @Override
    public void stop() {
        swarmClusterWrapper.getNodeNames().stream().forEach(swarmClusterWrapper::stop);
    }

    @Override
    public void destroy() {
        swarmClusterWrapper.destroy();
    }

    @Override
    public void verify() {
        List<DockerInfoWrapper> machines = swarmClusterWrapper.getNodeNames()
                .stream()
                .map(swarmClusterWrapper::getNode)
                .filter(x -> x.isStarted() || x.isReachable())
                .map(x -> new DockerInfoWrapper(swarmClusterWrapper.getDocker(x.getName()).daemonInfo(), x.getName()))
                .collect(Collectors.toList());

        //can we ssh to theese dockers
        machines
                .stream()
                .forEach(x -> swarmClusterWrapper.ssh(x.getName(), "echo ssh-test"));


        //is there at least one manager
        machines.stream()
                .filter(x -> x.isManager() && x.isSwarmActive())
                .findFirst().orElseThrow(() -> new RuntimeException("No Manager found"));

        //every machine must have swarm enabled
        machines.stream()
                .filter(x -> !x.isSwarmActive())
                .forEach(x -> {
                    throw new RuntimeException("All nodes must be swarm enabled");
                });

        //all must belong to same cluster and have same managers visibility
        List<Tuple> collect = machines.stream()
                .map(x -> new Tuple(new Object[]{x.isSwarmActive(), x.getRemoteManagers()}))
                .distinct().collect(Collectors.toList());
        if (collect.size() != 1 ||
                ((Map) collect.get(0).get(1)).size() != machines.stream().filter(x -> x.isManager()).count()) {
            throw new RuntimeException("Inconsistent swarm cluster detected");
        }

    }


    @Override
    public StackIntrospectionProvider getIntrospectionProvider() {
        return new SwarmStackIntrospectionProvider(swarmClusterWrapper.getDockerForManagementNode());
    }

    @Override
    public EnvironmentProvisionExecutor getProvisionExecutor() {
        return new DefaultProvisionerExecutor( this);
    }

    @Override
    public EnvironmentContainerRunner getContainerRunner() {
        return new SwarmContainerRunner(swarmClusterWrapper.getDockerForManagementNode());
    }

    @Override
    public EnvironmentContext getEnvironmentContext() {
        return environmentContext;
    }

    @Override
    public EnvironmentApiFacade getEnvironmentApiFacade() {
        return swarmClusterWrapper;
    }


    private File getDockerMachineStorageDir() {
        return environmentContext.getSettingsDirectory();
    }

    private SettingsImporterExporter getImporterExporter() {
        return machineSettingsImporterExporter;
    }

    private int getManagersCount() {
        return environmentContext.getEnvironmentDescription()
                .getParameterAsInt("managers")
                .orElse(1);
    }

    private int getWorkersCount() {
        return environmentContext.getEnvironmentDescription()
                .getParameterAsInt("workers")
                .orElse(0);
    }

    public NodeProvisioner getNodeProvisioner() {
        return nodeProvisioner;
    }
}
