package org.s4s0l.shathel.commons.ssh

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.util.concurrent.atomic.AtomicInteger

/**
 * @author Marcin Wielgus
 */
@TypeChecked
@CompileStatic
class SshTunelManagerImpl implements SshTunelManager {
    private static
    final Logger LOGGER = LoggerFactory.getLogger(SshTunelManagerImpl.class)
    private final File controlSocketsLocation
    private final String user
    private final int startPort
    private final SshWrapper ssh
    private static final Map<String, SshSharedState> connections = [:]
    private static final AtomicInteger controlFileNum = new AtomicInteger(0)

    SshTunelManagerImpl(File controlSocketsLocation, String user, int startPort, File knownHostsLocation) {
        this.controlSocketsLocation = controlSocketsLocation
        if (controlSocketsLocation.absolutePath.size() >= (108 - 18)) {
            throw new RuntimeException("Control socket location is too long, assumed max is 108 characters. Change the location of shathel solution to shorter path. see cat /usr/include/linux/un.h | grep \"define UNIX_PATH_MAX\" to see your limits")
        }
        this.user = user
        this.startPort = startPort
        this.ssh = new SshWrapper(knownHostsLocation)
    }

    int getNextPort() {
        int maxFound = startPort
        connections.each {
            if (!it.value.openedPorts.isEmpty()) {
                int maxportOpened = it.value.openedPorts.max()
                if (maxportOpened > maxFound) {
                    maxFound = maxportOpened
                }
            }
        }
        return maxFound + 1
    }


    @Override
    int openTunnel(File key, String sshHost, String sshTunnel) {
        synchronized (connections) {
            SshSharedState state = connections[sshHost] ?: new SshSharedState(user, sshHost, getControlSocketForHost(sshHost), key, ssh)
            def socket = state.tunnelSocket(sshTunnel, nextPort)
            connections[sshHost] = state
            return socket
        }

    }

    private File getControlSocketForHost(String sshHost) {
        new File(controlSocketsLocation, "${controlFileNum.incrementAndGet()}")
    }

    @Override
    synchronized void closeAllConnections(String sshHost) {
        synchronized (connections) {
            connections[sshHost]?.close()
            connections.remove(sshHost)
        }
    }

    @Override
    synchronized void closeAll() {
        synchronized (connections) {
            new HashSet<String>(connections.keySet()).each {
                closeAllConnections(it)
            }
            connections.clear()
        }
    }

    @Override
    void afterEnvironmentDestroyed() {
        closeAll()
        if (ssh.knownHostsLocation.exists()) {
            ssh.knownHostsLocation.delete()
        }
    }
}
