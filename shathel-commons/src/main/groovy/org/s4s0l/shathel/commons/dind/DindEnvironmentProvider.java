package org.s4s0l.shathel.commons.dind;

import org.s4s0l.shathel.commons.core.DefaultSettingsImporterExporter;
import org.s4s0l.shathel.commons.core.Parameters;
import org.s4s0l.shathel.commons.core.SolutionDescription;
import org.s4s0l.shathel.commons.core.environment.Environment;
import org.s4s0l.shathel.commons.core.environment.EnvironmentContext;
import org.s4s0l.shathel.commons.core.environment.EnvironmentDescription;
import org.s4s0l.shathel.commons.core.environment.EnvironmentProvider;
import org.s4s0l.shathel.commons.core.security.LazyInitiableSafeStorage;
import org.s4s0l.shathel.commons.core.security.SafeStorageProvider;
import org.s4s0l.shathel.commons.core.storage.Storage;
import org.s4s0l.shathel.commons.swarm.SwarmEnvironment;
import org.s4s0l.shathel.commons.swarm.SwarmNodeProvisioner;
import org.s4s0l.shathel.commons.utils.ExtensionContext;

/**
 * @author Matcin Wielgus
 */
public class DindEnvironmentProvider implements EnvironmentProvider {
    private final Parameters params;

    public DindEnvironmentProvider(Parameters params) {
        this.params = params;
    }

    @Override
    public String getType() {
        return "dind";
    }

    @Override
    public Environment getEnvironment(EnvironmentContext environmentContext) {
        DefaultSettingsImporterExporter machineSettingsImporterExporter = new DefaultSettingsImporterExporter();
        DindClusterWrapper clusterWrapper = new DindClusterWrapper(environmentContext);
        return new SwarmEnvironment(environmentContext, machineSettingsImporterExporter,
                clusterWrapper, new SwarmNodeProvisioner(params, clusterWrapper));
    }
}
