package org.s4s0l.shathel.commons.ssh

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import org.s4s0l.shathel.commons.utils.ExecutableResults

/**
 * @author Marcin Wielgus
 */
@TypeChecked
@CompileStatic
interface SshTunelManager {

    /**
     * Forwards a port to some local port.
     * Registers shutdown hook that will clean up the connections
     * @param key private key location
     * @param sshHost where are we connecting to (host:port)
     * @param sshTunnel what we want to forward (host:port)
     * @return number of port opened on interface 127.0.0.1
     */
    int openTunnel(File key, String sshHost, String sshTunnel)

    ExecutableResults exec(File key, String sshHost, String command)

    ExecutableResults sudo(File key, String sshHost, String command)

    void scp(File key, String sshHost, File localFile,String remotePath)

    void closeAllConnections(String sshHost)

    void closeAll()

    void afterEnvironmentDestroyed()

    String getRemoteUser()
}