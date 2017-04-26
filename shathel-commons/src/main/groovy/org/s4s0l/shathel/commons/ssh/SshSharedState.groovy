package org.s4s0l.shathel.commons.ssh

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import org.s4s0l.shathel.commons.utils.ExecutableResults
import org.s4s0l.shathel.commons.utils.IoUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.util.concurrent.atomic.AtomicBoolean
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
    private final AtomicBoolean hookRegistered = new AtomicBoolean(false)

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

    private synchronized void open() {
        LOGGER.warn("Opening SSH connection to ${host} with control socket ${controlSocket.absolutePath}")
        if (controlSocket.exists()) {
            throw new RuntimeException("Control socket ${controlSocket.absolutePath} already exists! Close connection manually please.")
        }
        openProcess = new Thread({
            String out
            try {
                if (hookRegistered.compareAndSet(false, true)) {
                    Runtime.addShutdownHook {
                        LOGGER.info("Shutdown hook triggered - assuring ssh tunnel is closed, ${host}:${port} via control socket: ${controlSocket}")
                        close()
                    }
                }
                out = sshWrapper.openConnection(user, host, port, key, controlSocket)
            } catch (Exception e) {
                LOGGER.debug("ssh connection open failed", e)
            } finally {
                handleSshClosed(out)
            }

        })
        openProcess.start()
        IoUtils.waitForFile(controlSocket, 10, new RuntimeException("Unable to open ssh connection, check logs"))
        Thread.sleep(1000)//todo find a better way
        opened = true
    }

    private synchronized void handleSshClosed(String output) {
        if (isOpened()) {
            def self = this
            LOGGER.warn("Unexpectedly closed ssh connection ${host}:${port} via control socket: ${controlSocket}, output: $output")
            new Thread({
                synchronized (self) {
                    LOGGER.warn("Trying to reopen ${host}:${port} via control socket: ${controlSocket}.")
                    if (!controlSocket.exists()) {
                        open()
                        restoreTunnels()
                    }else{
                        LOGGER.warn("WTF? Control socket exists so doing nothing for ${host}:${port} via control socket: ${controlSocket}.")
                    }
                }
            }).start()
        }
    }

    private synchronized restoreTunnels() {
        openedSockets.each {
            LOGGER.warn("Restoring tunnel 127.0.0.1:${it.value}:${it.key} via control socket: ${controlSocket}.")
            sshWrapper.tunnelConnection(host, port, controlSocket, "127.0.0.1:${it.value}:${it.key}")
        }
    }

    synchronized boolean isOpened() {
        opened
    }

    synchronized ExecutableResults sudo(String command) {
        if (!isOpened()) {
            open()
        }
        sshWrapper.sudo(host, port, controlSocket, command)
    }

    synchronized ExecutableResults exec(String command) {
        if (!isOpened()) {
            open()
        }
        sshWrapper.exec(host, port, controlSocket, command)
    }

    synchronized void scp(File file, String remotePath) {
        if (!isOpened()) {
            open()
        }
        sshWrapper.scp(controlSocket, file, remotePath)

    }

    synchronized int tunnelSocket(String target, int localPortToUse) {
        if (openedSockets.get(target) != null) {
            return openedSockets.get(target).intValue()
        }
        if (!isOpened()) {
            open()
        }
        sshWrapper.tunnelConnection(host, port, controlSocket, "127.0.0.1:${localPortToUse}:${target}")
        openedSockets.put(target, localPortToUse)
        return localPortToUse

    }


    @Override
    synchronized void close() throws IOException {
        try {
            if (isOpened() || controlSocket.exists()) {
                LOGGER.warn("Closing ssh connection to ${host}:${port} via control socket: ${controlSocket}")
                sshWrapper.closeConnection(host, port, controlSocket)
            }
        } finally {
            openedSockets.clear()
            opened = false
        }

    }


}
