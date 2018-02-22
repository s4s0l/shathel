package org.s4s0l.shathel.commons.remoteswarm

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import org.s4s0l.shathel.commons.scripts.TypedScript

/**
 * @author Marcin Wielgus
 */
@TypeChecked
@CompileStatic
class RemoteEnvironmentScript implements TypedScript {

    final String type
    final String name
    final String envPackageGav
    final File baseDirectory

    RemoteEnvironmentScript(String type, String name, String envPackageGav, File baseDirectory) {
        this.type = type
        this.name = name
        this.envPackageGav = envPackageGav
        this.baseDirectory = baseDirectory
    }

    @Override
    String getType() {
        return type
    }

    @Override
    String getScriptContents() {
        getScriptFileLocation().get().text
    }

    @Override
    String getScriptName() {
        return getType() + ":" + envPackageGav + "/" + name
    }

    File getBaseDirectory() {
        return baseDirectory
    }

    @Override
    Optional<File> getScriptFileLocation() {
        return Optional.of(new File(baseDirectory, name))
    }
}
