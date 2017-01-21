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
import java.util.Optional;

/**
 * @author Matcin Wielgus
 */
public class MachineEnvironment implements Environment {

    private final String solutionName;
    private final File temporaryDir;
    private final SafeStorage safeStorage;
    private final EnvironmentDescription environmentDescription;


    public MachineEnvironment(String solutionName, File temporaryDir, SafeStorage safeStorage,
                              EnvironmentDescription environmentDescription,
                              MachineProvisioner machineProvisioner) {
        this.solutionName = solutionName;
        this.temporaryDir = temporaryDir;
        this.safeStorage = safeStorage;
        this.environmentDescription = environmentDescription;
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
        if (dm.getManagerNodeNames().size() != getManagersCount()) {
            return false;
        }
        if (dm.getWorkerNodeNames().size() != getWorkersCount()) {
            return false;
        }
        return true;
    }

    @Override
    public void initialize() {
        try {
            FileUtils.deleteDirectory(getDockerMachineStorageDir());
            String safeStoreKey = getSafeStorageKey();
            Optional<InputStream> inputStream = safeStorage.inputStream(safeStoreKey);
            if (inputStream.isPresent()) {
                getImporterExporter().loadSettings(inputStream.get(), getDockerMachineStorageDir());
            } else {
                getDockerMachineStorageDir().mkdirs();
            }

            boolean changed = getMachineProvisioner().createMachines(getDockerMachineStorageDir(),
                    getBaseMachineName(),
                    environmentDescription.getName(), getManagersCount(), getWorkersCount());

            if (changed) {
                getImporterExporter().saveSettings(getDockerMachineStorageDir(),
                        safeStorage.outputStream(safeStoreKey));
            }
        } catch (Exception e) {
            throw new RuntimeException("Initialization failed", e);
        }
    }

    private String getBaseMachineName() {
        return solutionName + "." + environmentDescription.getName();
    }

    private String getSafeStorageKey() {
        return environmentDescription.getName() + "/MACHINES";
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
            throw new RuntimeException("Not initialized");
        }
        DockerMachineCommons dmc = getDockerMachineCommons();
        for (String machine : dmc.getAllNodeNames()) {
            dmc.testConnectivity(machine);
        }
        for (String machine : dmc.getManagerNodeNames()) {
            dmc.testIsManager(machine);
        }
        for (String machine : dmc.getWorkerNodeNames()) {
            dmc.testIsWorker(machine);
        }
        dmc.testSwarmStatus();
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


    private File getDockerMachineStorageDir() {
        return new File(temporaryDir, "settings");
    }

    private DockerMachineCommons getDockerMachineCommons() {
        return new DockerMachineCommons(getBaseMachineName(), getDockerMachineStorageDir());
    }

    private MachineSettingsImporterExporter getImporterExporter() {
        return new MachineSettingsImporterExporter(new File(temporaryDir, "importExportTmp"));
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
