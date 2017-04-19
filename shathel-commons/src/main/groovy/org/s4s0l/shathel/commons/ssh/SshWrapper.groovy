package org.s4s0l.shathel.commons.ssh

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import org.s4s0l.shathel.commons.utils.ExecWrapper
import org.s4s0l.shathel.commons.utils.ExecutableResults
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
    private final ExecWrapper scp = new ExecWrapper(LOGGER, "scp", [:])
    final File knownHostsLocation

    SshWrapper() {
        knownHostsLocation = null
    }

    SshWrapper(File knownHostsLocation) {
        this.knownHostsLocation = knownHostsLocation
    }

    private String getKnownHosts() {
        return knownHostsLocation == null ? "" : "-o UserKnownHostsFile=${knownHostsLocation.absolutePath}"
    }

    void openConnection(String user, String host, int port, File key, File controlSocket) {
        exec.executeForOutput("${knownHosts} -oStrictHostKeyChecking=no -f -i ${key.absolutePath} -M -S ${controlSocket.absolutePath} -l ${user} -N  -p ${port} ${host}")
    }

    void closeConnection(String host, int port, File controlSocket) {
        exec.executeForOutput("-S ${controlSocket.absolutePath} -O exit -p ${port} ${host} ")
    }

    void tunnelConnection(String host, int port, File controlSocket, String mapString) {
        exec.executeForOutput("-f  -S ${controlSocket.absolutePath} -L ${mapString} -N -p ${port} ${host} ")
    }

    ExecutableResults exec(String host, int port, File controlSocket, String command) {
        return exec.execute("-S", "${controlSocket.absolutePath}", "-p", "${port}", "${host}", "bash", "-c", "\"${command}\"")
    }

    ExecutableResults sudo(String host, int port, File controlSocket, String command) {
        return exec.execute("-S", "${controlSocket.absolutePath}", "-p", "${port}", "${host}", "sudo", "bash", "-c", "\"${command}\"")
    }

    void scp(File controlSocket, File localFile, String remote){
        scp.executeForOutput("-o ControlPath=${controlSocket.absolutePath} ${localFile.absolutePath} remote:${remote}")
    }

}
