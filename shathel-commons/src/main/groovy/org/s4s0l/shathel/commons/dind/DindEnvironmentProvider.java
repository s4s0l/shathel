package org.s4s0l.shathel.commons.dind;

import org.s4s0l.shathel.commons.core.DefaultSettingsImporterExporter;
import org.s4s0l.shathel.commons.core.environment.Environment;
import org.s4s0l.shathel.commons.core.environment.EnvironmentContext;
import org.s4s0l.shathel.commons.core.environment.EnvironmentProvider;
import org.s4s0l.shathel.commons.swarm.SwarmEnvironment;
import org.s4s0l.shathel.commons.swarm.SwarmNodeProvisioner;

/**
 * @author Marcin Wielgus
 */
public class DindEnvironmentProvider implements EnvironmentProvider {


    @Override
    public String getType() {
        return "dind";
    }

    @Override
    public Environment getEnvironment(EnvironmentContext environmentContext) {
        DefaultSettingsImporterExporter machineSettingsImporterExporter = new DefaultSettingsImporterExporter();
        DindClusterWrapper clusterWrapper = new DindClusterWrapper(environmentContext);
        return new SwarmEnvironment(environmentContext, machineSettingsImporterExporter,
                clusterWrapper, new SwarmNodeProvisioner(clusterWrapper, clusterWrapper));
    }
}
