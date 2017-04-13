package org.s4s0l.shathel.commons.ssh

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import org.s4s0l.shathel.commons.utils.ExecWrapper
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * @author Marcin Wielgus
 */
@TypeChecked
@CompileStatic
class SshWrapper {
    private static final Logger LOGGER = LoggerFactory.getLogger(SshWrapper.class)
    private final ExecWrapper exec = new ExecWrapper(LOGGER, "ssh", [:])
    private final File knownHostsLocation

    SshWrapper() {
        knownHostsLocation = null
    }

    SshWrapper(File knownHostsLocation) {
        this.knownHostsLocation = knownHostsLocation
    }

    private String getKnownHosts(){
        return knownHostsLocation == null ? "" : "-o UserKnownHostsFile=${knownHostsLocation.absolutePath}"
    }

    void openConnection(String user, String host, int port, File key, File controlSocket) {
        exec.executeForOutput("${knownHosts} -oStrictHostKeyChecking=no -f -i ${key.absolutePath} -M -S ${controlSocket.absolutePath} -l ${user} -N  -p ${port} ${host}")
    }

    void closeConnection(String host, int port, File controlSocket) {
        exec.executeForOutput("${knownHosts} -oStrictHostKeyChecking=no -S ${controlSocket.absolutePath} -O exit -p ${port} ${host} ")
    }

    void tunnelConnection(String user, String host, int port,  File key, File controlSocket, String mapString) {
        exec.executeForOutput("${knownHosts} -oStrictHostKeyChecking=no -f -i ${key.absolutePath} -S ${controlSocket.absolutePath} -l ${user} -L ${mapString} -N -p ${port} ${host} ")
    }

}
