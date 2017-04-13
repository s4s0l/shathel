package org.s4s0l.shathel.commons.remoteswarm

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import org.s4s0l.shathel.commons.core.environment.ShathelNode

/**
 * Wrapper for vagrant inventory file.
 * Knows machines its names and addresses
 * @author Marcin Wielgus
 */
@TypeChecked
@CompileStatic
class RemoteEnvironmentInventoryFile {
    final File inventoryFileLocation;

    RemoteEnvironmentInventoryFile(File inventoryFileLocation) {
        this.inventoryFileLocation = inventoryFileLocation
    }

    List<ShathelNode> getNodes(){

    }
}
