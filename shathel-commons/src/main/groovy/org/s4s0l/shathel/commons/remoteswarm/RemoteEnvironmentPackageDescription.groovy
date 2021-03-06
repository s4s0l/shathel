package org.s4s0l.shathel.commons.remoteswarm

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked;
import org.s4s0l.shathel.commons.core.model.EnvironmentFileModel;
import org.s4s0l.shathel.commons.core.stack.StackReference

/**
 * @author Marcin Wielgus
 */
@TypeChecked
@CompileStatic
class RemoteEnvironmentPackageDescription {

    private EnvironmentFileModel envPackageFileModel
    private File envPackageRootDirectory

    RemoteEnvironmentPackageDescription(EnvironmentFileModel envPackageFileModel, File envPackageRootDirectory) {
        this.envPackageFileModel = envPackageFileModel
        this.envPackageRootDirectory = envPackageRootDirectory
    }

    String getGav() {
        envPackageFileModel.getGav()
    }

    String getVersion() {
        return new StackReference(gav).getVersion()
    }

    Map<String, String> getMandatoryEnvs() {
        return envPackageFileModel.mandatoryEnvironmentVariables
    }

    String getRemoteUser() {
        return envPackageFileModel.imageUser
    }

    File getPackageRootDirectory() {
        return envPackageRootDirectory
    }


    private RemoteEnvironmentScript toScript(Map<String, String> map) {
        return new RemoteEnvironmentScript(map["type"], map["name"], envPackageFileModel.gav, getPackageRootDirectory())
    }

    RemoteEnvironmentScript getImagePreparationScript() {
        return toScript(envPackageFileModel.imagePreparationScript)
    }

    RemoteEnvironmentScript getInfrastructureScript() {
        return toScript(envPackageFileModel.infrastructureScript)
    }

    RemoteEnvironmentScript getSetupScript() {
        return toScript(envPackageFileModel.setupScript)
    }

    RemoteEnvironmentScript getSwarmScript() {
        return toScript(envPackageFileModel.swarmScript)
    }

    String getEnvPackageImage() {
        return "shathel-env-${version}".replaceAll("[^A-Za-z0-9-_]", "_")
    }
}
