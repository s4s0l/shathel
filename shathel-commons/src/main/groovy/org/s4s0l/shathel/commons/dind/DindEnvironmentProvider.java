package org.s4s0l.shathel.commons.dind;

import com.google.common.base.Supplier;
import org.s4s0l.shathel.commons.core.DefaultSettingsImporterExporter;
import org.s4s0l.shathel.commons.core.Parameters;
import org.s4s0l.shathel.commons.core.SolutionDescription;
import org.s4s0l.shathel.commons.core.environment.Environment;
import org.s4s0l.shathel.commons.core.environment.EnvironmentContext;
import org.s4s0l.shathel.commons.core.environment.EnvironmentDescription;
import org.s4s0l.shathel.commons.core.environment.EnvironmentProvider;
import org.s4s0l.shathel.commons.core.security.LazyInitiableSafeStorage;
import org.s4s0l.shathel.commons.core.security.SafeStorage;
import org.s4s0l.shathel.commons.core.security.SafeStorageProvider;
import org.s4s0l.shathel.commons.core.storage.Storage;
import org.s4s0l.shathel.commons.swarm.SwarmEnvironment;
import org.s4s0l.shathel.commons.swarm.SwarmNodeProvisioner;
import org.s4s0l.shathel.commons.utils.ExtensionContext;

import java.io.File;
import java.util.Optional;

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
    public Environment getEnvironment(Storage s, EnvironmentDescription environmentDescription,
                                      ExtensionContext ctxt, SolutionDescription solutionDescription) {
        String name = environmentDescription.getName();
        Optional<SafeStorageProvider> safeStorageProvider = ctxt.lookupOne(SafeStorageProvider.class);
        LazyInitiableSafeStorage safeStorage = new LazyInitiableSafeStorage(() ->
                safeStorageProvider.get().getSafeStorage(s, name));

        File rootEnvironmentDir = s.getTemporaryDirectory(name);
        EnvironmentContext context = new EnvironmentContext(environmentDescription, solutionDescription,
                safeStorage, rootEnvironmentDir);

        DefaultSettingsImporterExporter machineSettingsImporterExporter = new DefaultSettingsImporterExporter();

        DindClusterWrapper clusterWrapper = new DindClusterWrapper(context);

        return new SwarmEnvironment(context, machineSettingsImporterExporter,
                clusterWrapper, new SwarmNodeProvisioner(params, clusterWrapper));
    }
}
