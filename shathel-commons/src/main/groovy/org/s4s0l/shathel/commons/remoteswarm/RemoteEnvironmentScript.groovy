package org.s4s0l.shathel.commons.remoteswarm

import org.s4s0l.shathel.commons.scripts.TypedScript

/**
 * @author Marcin Wielgus
 */
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
        throw new UnsupportedOperationException("Not yet")
    }

    @Override
    String getScriptName() {
        return getType() + ":" + envPackageGav + "/" + name
    }

    @Override
    File getBaseDirectory() {
        return baseDirectory
    }

    @Override
    Optional<File> getScriptFileLocation() {
        return new File(baseDirectory, name)
    }
}
