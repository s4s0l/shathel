package org.s4s0l.shathel.commons.ssh

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import org.s4s0l.shathel.commons.utils.IoUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference

/**
 * @author Marcin Wielgus
 */
@TypeChecked
@CompileStatic
class SshSharedState implements Closeable {
    private static
    final Logger LOGGER = LoggerFactory.getLogger(SshSharedState.class);
    private final SshWrapper sshWrapper
    private final String host
    private final String user
    private final File controlSocket
    private final File key
    private final int port
    private Map<String, Integer> openedSockets = new HashMap<>()
    private boolean opened = false
    private Thread openProcess

    SshSharedState(String user, String host, File controlSocket, File key, SshWrapper sshWrapper) {
        this.user = user
        this.controlSocket = controlSocket
        this.key = key
        this.sshWrapper = sshWrapper
        if (host.contains(":")) {
            this.host = host.split(":")[0]
            this.port = Integer.parseInt(host.split(":")[1])
        } else {
            this.host = host
            this.port = 22
        }
    }

    Collection<Integer> getOpenedPorts() {
        return openedSockets.values()
    }

    synchronized void open() {
        if (isOpened()) {
            return
        }
        if(controlSocket.exists()){
            throw new RuntimeException("Control socket ${controlSocket.absolutePath} already exists! Close connection manually please.")
        }
        openProcess = new Thread({
            try {
                Runtime.addShutdownHook {
                    LOGGER.info("Shutdown hook triggered - assuring ssh tunnel is closed, ${host}:${port} via control socket: ${controlSocket}")
                    close()
                }
                sshWrapper.openConnection(user, host, port, key, controlSocket)
            } catch (Exception e) {
                LOGGER.error("ssh connection open failed", e)
            }
            closeInternal()
            LOGGER.info("ssh connection closed")
        })
        openProcess.start()
        IoUtils.waitForFile(controlSocket, 10, new RuntimeException("Unable to open ssh connection, check logs"))
        Thread.sleep(1000)//todo find a better way
        opened = true
    }

    synchronized boolean isOpened() {
        opened
    }

    synchronized int tunnelSocket(String target, int localPortToUse) {
        if (openedSockets.get(target) != null) {
            return openedSockets.get(target).intValue()
        }
        if (!isOpened()) {
            open()
        }
        def nextPortToUse = localPortToUse
        sshWrapper.tunnelConnection(user, host, port, key, controlSocket, "127.0.0.1:${nextPortToUse}:${target}")
        openedSockets.put(target, nextPortToUse)
        return nextPortToUse

    }

    synchronized void closeInternal() {
        openedSockets.clear()
        opened = false
    }

    @Override
    synchronized void close() throws IOException {
        if (isOpened()) {
            LOGGER.info("Closing ssh connection to ${host}:${port} via control socket: ${controlSocket}")
            sshWrapper.closeConnection(host, port, controlSocket)
        }
        openedSockets.clear()
        opened = false
    }
}
