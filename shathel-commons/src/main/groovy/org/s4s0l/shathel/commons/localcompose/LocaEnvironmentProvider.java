package org.s4s0l.shathel.commons.localcompose;

import org.s4s0l.shathel.commons.core.Parameters;
import org.s4s0l.shathel.commons.core.SolutionDescription;
import org.s4s0l.shathel.commons.core.environment.*;
import org.s4s0l.shathel.commons.core.provision.EnvironmentProvisionExecutor;
import org.s4s0l.shathel.commons.core.storage.Storage;
import org.s4s0l.shathel.commons.utils.ExtensionContext;

import java.io.File;

/**
 * @author Matcin Wielgus
 */
public class LocaEnvironmentProvider implements EnvironmentProvider {


    @Override
    public String getType() {
        return "docker-compose";
    }

    @Override
    public Environment getEnvironment(Storage s, EnvironmentDescription environmentDescription,
                                      ExtensionContext ctxt,SolutionDescription solutionDescription) {
        String name = environmentDescription.getName();
        return new LocalEnvironment(s.getTemporaryDirectory(name),
                s.getWorkDirectory(name));
    }


}
