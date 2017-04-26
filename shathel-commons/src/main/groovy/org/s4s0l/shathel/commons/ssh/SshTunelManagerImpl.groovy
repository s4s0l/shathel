package org.s4s0l.shathel.commons.ssh

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import org.s4s0l.shathel.commons.utils.ExecutableResults
import org.s4s0l.shathel.commons.utils.IoUtils
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
        int candidate = maxFound + 1
        for (int i = 0; i < 100; i++) {
            if (!IoUtils.isSocketOpened("127.0.0.1", candidate + i, 500)) {
                LOGGER.debug("Socket ${candidate + i} seems to be free.")
                return candidate + i
            } else {
                LOGGER.warn("Socket ${candidate + i} seems to be taken...")
            }
        }
        throw new RuntimeException("No free ports starting from $candidate found!")
    }

    @Override
    ExecutableResults exec(File key, String sshHost, String command) {
        synchronized (connections) {
            SshSharedState state = getSharedState(sshHost, key)
            return state.exec(command)
        }
    }

    @Override
    void scp(File key, String sshHost, File localFile, String remotePath) {

        synchronized (connections) {
            SshSharedState state = getSharedState(sshHost, key)
            state.scp(localFile, remotePath)
        }
    }

    @Override
    ExecutableResults sudo(File key, String sshHost, String command) {
        synchronized (connections) {
            SshSharedState state = getSharedState(sshHost, key)
            return state.sudo(command)
        }
    }


    private SshSharedState getSharedState(String sshHost, File key) {
        SshSharedState ret = connections[sshHost] ?: new SshSharedState(user, sshHost, getControlSocketForHost(sshHost), key, ssh)
        connections[sshHost] = ret
        ret
    }

    @Override
    int openTunnel(File key, String sshHost, String sshTunnel) {
        synchronized (connections) {
            SshSharedState state = getSharedState(sshHost, key)
            def socket = state.tunnelSocket(sshTunnel, nextPort)
            return socket
        }

    }

    private File getControlSocketForHost(String sshHost) {
        new File(controlSocketsLocation, "${controlFileNum.incrementAndGet()}")
    }

    @Override
    void closeAllConnections(String sshHost) {
        synchronized (connections) {
            connections[sshHost]?.close()
            connections.remove(sshHost)
        }
    }

    @Override
    void closeAll() {
        synchronized (connections) {
            new HashSet<String>(connections.keySet()).each {
                closeAllConnections(it)
            }
            connections.clear()
        }
    }

    static void globalCloseAll() {
        synchronized (connections) {
            new HashSet<String>(connections.keySet()).each {
                connections[it]?.close()
                connections.remove(it)
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

    @Override
    String getRemoteUser() {
        return user
    }
}
