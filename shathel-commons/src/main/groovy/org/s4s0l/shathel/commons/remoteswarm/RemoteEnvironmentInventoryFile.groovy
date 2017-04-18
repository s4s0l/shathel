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

    List<ShathelNode> getNodes() {
        def ret = []
        if (!inventoryFileLocation.exists()) {
            return ret
        }
        def hostsMatch = inventoryFileLocation.text =~ /([^\s]+)\s+((?:(?:(?:private_ip|public_ip|shathel_name|shathel_role)=[^\s]+).*){4})/
        while (hostsMatch.find()) {
            def host = [name: hostsMatch.group(1)]
            def allAtts = hostsMatch.group(2)
            def attMatch = allAtts =~ /(private_ip|public_ip|shathel_name|shathel_role)+\s*=\s*([^\s]+)/
            while (attMatch.find()) {
                host.putAll([(attMatch.group(1)): attMatch.group(2)])
            }
            ret.add(new ShathelNode(host.shathel_name, host.public_ip, host.private_ip, host.shathel_role))
        }
        ret
    }

    void afterEnvironmentDestroyed() {
        if (inventoryFileLocation.exists()) {
            inventoryFileLocation.delete()
        }
    }
}
