package org.s4s0l.shathel.commons.ssh

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked

import java.util.concurrent.atomic.AtomicInteger

/**
 * @author Marcin Wielgus
 */
@TypeChecked
@CompileStatic
class SshTunelManagerImpl implements SshTunelManager {

    private final File controlSocketsLocation
    private final String user
    private final AtomicInteger port
    private final SshWrapper ssh
    private Map<String, SshSharedState> connections = [:]

    SshTunelManagerImpl(File controlSocketsLocation, String user, int startPort, File knownHostsLocation) {
        this.controlSocketsLocation = controlSocketsLocation
        this.user = user
        this.port = new AtomicInteger(startPort)
        this.ssh = new SshWrapper(knownHostsLocation)
    }

    @Override
    synchronized int openTunnel(File key, String sshHost, String sshTunnel) {
        SshSharedState state = connections[sshHost] ?: new SshSharedState(user, sshHost, getControlSocketForHost(sshHost), key, ssh)
        def socket = state.tunnelSocket(sshTunnel, port)
        connections[sshHost] = state
        return socket

    }

    private File getControlSocketForHost(String sshHost) {
        new File(controlSocketsLocation, sshHost.replaceAll("[^A-Za-z0-9]", "_"))
    }

    @Override
    synchronized void closeAllConnections(String sshHost) {
        connections[sshHost]?.close()
        connections.remove(sshHost)
    }

    @Override
    synchronized void closeAll() {
        connections.keySet().each {
            closeAllConnections(it)
        }
        connections = [:]
    }
}
