package org.s4s0l.shathel.commons.ssh

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked

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

    void closeAllConnections(String sshHost)

    void closeAll()

    void afterEnvironmentDestroyed()


}