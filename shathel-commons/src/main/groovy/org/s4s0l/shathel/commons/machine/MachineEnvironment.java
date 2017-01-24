package org.s4s0l.shathel.commons.machine;

import org.apache.commons.io.FileUtils;
import org.s4s0l.shathel.commons.core.environment.Environment;
import org.s4s0l.shathel.commons.core.environment.EnvironmentContainerRunner;
import org.s4s0l.shathel.commons.core.environment.EnvironmentDescription;
import org.s4s0l.shathel.commons.core.environment.StackIntrospectionProvider;
import org.s4s0l.shathel.commons.core.provision.EnvironmentProvisionExecutor;
import org.s4s0l.shathel.commons.core.security.SafeStorage;

import java.io.File;
import java.io.InputStream;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author Matcin Wielgus
 */
public class MachineEnvironment implements Environment {

    private final String solutionName;
    private final File temporaryDir;
    private final SafeStorage safeStorage;
    private final EnvironmentDescription environmentDescription;
    private final MachineSettingsImporterExporter machineSettingsImporterExporter;


    public MachineEnvironment(String solutionName, File temporaryDir, SafeStorage safeStorage,
                              EnvironmentDescription environmentDescription,
                              MachineSettingsImporterExporter machineSettingsImporterExporter,
                              MachineProvisioner machineProvisioner) {
        this.solutionName = solutionName;
        this.temporaryDir = temporaryDir;
        this.safeStorage = safeStorage;
        this.environmentDescription = environmentDescription;
        this.machineSettingsImporterExporter = machineSettingsImporterExporter;
        this.machineProvisioner = machineProvisioner;
    }

    private final MachineProvisioner machineProvisioner;


    @Override
    public File getExecutionDirectory() {
        File execution = new File(temporaryDir, "execution");
        execution.mkdirs();
        return execution;
    }


    @Override
    public boolean isInitialized() {
        if (!getDockerMachineStorageDir().exists()) {
            return false;
        }
        DockerMachineCommons dm = getDockerMachineCommons();
        Map<DockerMachineCommons.Type, List<String>> machines = dm.getMachines();
        if (machines.get(DockerMachineCommons.Type.MANAGER).size() < getManagersCount()) {
            return false;
        }
        if (machines.get(DockerMachineCommons.Type.WORKER).size() < getWorkersCount()) {
            return false;
        }
        return true;
    }

    @Override
    public void initialize() {
        stop();
        try {
            FileUtils.deleteDirectory(getDockerMachineStorageDir());
            String safeStoreKey = getSafeStorageKey();
            Optional<InputStream> inputStream = safeStorage.inputStream(safeStoreKey);
            if (inputStream.isPresent()) {
                //todo tu brakuje czegos w rodzaju reload settings
                //co by odrejestrowało istniejace maszyny z vboxa aby mu nie robic syfu
                getImporterExporter().loadSettings(inputStream.get(),
                        getDockerMachineStorageDir());
                start();
            } else {
                getDockerMachineStorageDir().mkdirs();
            }

            boolean changed = getMachineProvisioner().createMachines(
                    getDockerMachineStorageDir(),
                    getBaseMachineName(),
                    environmentDescription.getName(),
                    getManagersCount(),
                    getWorkersCount());

            if (changed) {
                getImporterExporter().saveSettings(getDockerMachineStorageDir(),
                        safeStorage.outputStream(safeStoreKey));
            }
        } catch (Exception e) {
            throw new RuntimeException("Initialization failed", e);
        }
    }

    private String getBaseMachineName() {
        return solutionName + "-" + environmentDescription.getName();
    }

    private String getSafeStorageKey() {
        return "machines";
    }

    @Override
    public void start() {
        getDockerMachineCommons().startAll();
    }

    @Override
    public boolean isStarted() {
        return getDockerMachineCommons().isAllStarted();
    }

    @Override
    public void stop() {
        getDockerMachineCommons().stopAll();
    }

    @Override
    public void destroy() {
        getDockerMachineCommons().destroyAll();
        getImporterExporter().saveSettings(getDockerMachineStorageDir(),
                safeStorage.outputStream(getSafeStorageKey()));
    }

    @Override
    public void verify() {
        if (!isInitialized()) {
            throw new RuntimeException("Environment is not initialized");
        }
        DockerMachineCommons dmc = getDockerMachineCommons();
        Map<DockerMachineCommons.Type, List<String>> machines = dmc.getMachines();

        machines.entrySet()
                .stream()
                .map(Map.Entry::getValue)
                .flatMap(Collection::stream)
                .forEach(dmc::testConnectivity);
        machines.get(DockerMachineCommons.Type.MANAGER)
                .stream()
                .forEach(dmc::testIsManager);
        machines.get(DockerMachineCommons.Type.WORKER)
                .stream()
                .forEach(dmc::testIsWorker);
        dmc.testSwarmStatus();
    }

    @Override
    public StackIntrospectionProvider getIntrospectionProvider() {
        return new MachineStackIntrospectionProvider(getDockerMachineCommons());
    }

    @Override
    public EnvironmentProvisionExecutor getProvisionExecutor() {
        return new MachineEnvironmentProvisionExecutor();
    }

    @Override
    public EnvironmentContainerRunner getContainerRunner() {
        return new MachineEnvironmentContainerRunner(getDockerMachineCommons());
    }


    private File getDockerMachineStorageDir() {
        return new File(temporaryDir, "settings");
    }

    private DockerMachineCommons getDockerMachineCommons() {
        return new DockerMachineCommons(getBaseMachineName(),
                getDockerMachineStorageDir());
    }


    private MachineSettingsImporterExporter getImporterExporter() {
        return machineSettingsImporterExporter;
    }

    private int getManagersCount() {
        return environmentDescription
                .getParameterAsInt("managers")
                .orElse(1);
    }

    private int getWorkersCount() {
        return environmentDescription
                .getParameterAsInt("workers")
                .orElse(0);
    }

    public MachineProvisioner getMachineProvisioner() {
        return machineProvisioner;
    }
}
