package org.s4s0l.shathel.commons.remoteswarm

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import org.s4s0l.shathel.commons.core.SettingsImporterExporter
import org.s4s0l.shathel.commons.core.environment.*
import org.s4s0l.shathel.commons.scripts.NamedExecutable
import org.s4s0l.shathel.commons.scripts.ansible.AnsibleScriptContext
import org.s4s0l.shathel.commons.secrets.SecretsEnricher
import org.s4s0l.shathel.commons.swarm.*

/**
 * @author Marcin Wielgus
 */
@TypeChecked
@CompileStatic
class RemoteEnvironment implements Environment {
    private final SettingsImporterExporter machineSettingsImporterExporter
    private final RemoteEnvironmentPackageContext packageContext
    private final RemoteEnvironmentController controller
    private final RemoteEnvironmentApiFacade apiFacade

    RemoteEnvironment(SettingsImporterExporter machineSettingsImporterExporter,
                      RemoteEnvironmentPackageContext packageContext,
                      RemoteEnvironmentController controller,
                      RemoteEnvironmentApiFacade apiFacade) {
        this.machineSettingsImporterExporter = machineSettingsImporterExporter
        this.packageContext = packageContext
        this.controller = controller
        this.apiFacade = apiFacade
    }

    @Override
    boolean isInitialized() {
        return controller.isInitialized()
    }

    @Override
    void initialize() {
        controller.initialize()
    }

    @Override
    void start() {
        controller.start()
    }

    @Override
    boolean isStarted() {
        return controller.isStarted()
    }

    @Override
    void stop() {
        controller.stop()
    }

    @Override
    void destroy() {
        controller.destroy()
    }

    @Override
    void verify() {
        controller.verify()
    }

    private static String getSafeStorageKey() {
        return "machines"
    }

    @Override
    void save() {
        String safeStoreKey = getSafeStorageKey()
        machineSettingsImporterExporter.saveSettings(packageContext.settingsDirectory,
                packageContext.getSafeStorage().outputStream(safeStoreKey))
    }

    @Override
    void load() {
        String safeStoreKey = getSafeStorageKey()
        Optional<InputStream> inputStream = packageContext.getSafeStorage().inputStream(safeStoreKey)
        if (inputStream.isPresent()) {
            if (packageContext.getAnsibleInventoryFile().exists() && isStarted()) {
                stop()
            }
            machineSettingsImporterExporter.loadSettings(inputStream.get(),
                    packageContext.settingsDirectory)
        } else {
            throw new RuntimeException("No saved state found!")
        }
    }

    @Override
    StackIntrospectionProvider getIntrospectionProvider() {
        return new SwarmStackIntrospectionProvider(getEnvironmentApiFacade().getManagerNodeWrapper())
    }

    @Override
    EnvironmentContainerRunner getContainerRunner() {
        return new SwarmContainerRunner(getEnvironmentApiFacade().getManagerNodeWrapper(), getEnvironmentContext().dockerLoginInfo)
    }

    @Override
    EnvironmentContext getEnvironmentContext() {
        return packageContext
    }

    @Override
    ExecutableApiFacade getEnvironmentApiFacade() {
        return apiFacade
    }

    @Override
    List<NamedExecutable> getEnvironmentEnrichers() {
        //TODO: register enrichers
        return Arrays.<NamedExecutable> asList(
                //TODO: parametrize remote data directory - should come from envdesc
                new SwarmMountingPermissionsEnricher("/shathel-data", this.apiFacade.sshOperations),
                new SwarmMountingEnricher("/shathel-data", this.apiFacade.sshOperations),
                new SwarmPullingEnricher(),
                new SecretsEnricher(),
                new MandatoryEnvironmentsValidator(),
                new SwarmStickyVolumeEnricher()
        )
    }

    @Override
    AnsibleScriptContext getAnsibleScriptContext() {
        return new AnsibleScriptContext(
                packageContext.remoteUser,
                new File(packageContext.keysDirectory, "id_rsa"),
                packageContext.ansibleInventoryFile,
                packageContext.knownHostsFile
        )
    }
}
