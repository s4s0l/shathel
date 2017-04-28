package org.s4s0l.shathel.commons.localswarm

import org.s4s0l.shathel.commons.core.DefaultSettingsImporterExporter
import org.s4s0l.shathel.commons.core.environment.Environment
import org.s4s0l.shathel.commons.core.environment.EnvironmentContext
import org.s4s0l.shathel.commons.core.environment.EnvironmentProvider

/**
 * @author Marcin Wielgus
 */
class LocalSwarmEnvironmentProvider implements EnvironmentProvider {


    @Override
    String getType() {
        return "local-swarm"
    }

    @Override
    Environment getEnvironment(EnvironmentContext environmentContext) {
        DefaultSettingsImporterExporter machineSettingsImporterExporter = new DefaultSettingsImporterExporter()
        return new LocalSwarmEnvironment(new LocalSwarmEnvironmentContextImpl(environmentContext), machineSettingsImporterExporter)
    }


}