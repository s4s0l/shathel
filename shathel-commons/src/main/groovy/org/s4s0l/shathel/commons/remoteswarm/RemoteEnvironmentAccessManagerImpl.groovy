package org.s4s0l.shathel.commons.remoteswarm

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import org.s4s0l.shathel.commons.cert.CertificateManager
import org.s4s0l.shathel.commons.cert.KeyCert
import org.s4s0l.shathel.commons.core.environment.ShathelNode
import org.s4s0l.shathel.commons.ssh.SshKeyProvider
import org.s4s0l.shathel.commons.ssh.SshTunelManager

/**
 * Knows how to access nodes in InventoryFile
 * @author Marcin Wielgus
 */
@TypeChecked
@CompileStatic
class RemoteEnvironmentAccessManagerImpl implements RemoteEnvironmentAccessManager {
    private final RemoteEnvironmentInventoryFile inventoryFile
    private final CertificateManager certificateManager
    private final SshKeyProvider sshKeyProvider
    private final SshTunelManager tunnelManager

    RemoteEnvironmentAccessManagerImpl(RemoteEnvironmentInventoryFile inventoryFile,
                                       CertificateManager certificateManager,
                                       SshKeyProvider sshKeyProvider,
                                       SshTunelManager tunnelManager) {
        this.inventoryFile = inventoryFile
        this.certificateManager = certificateManager
        this.sshKeyProvider = sshKeyProvider
        this.tunnelManager = tunnelManager
    }

    @Override
    void checkPreConditions() {
        sshKeyProvider.keys
        certificateManager.clientCerts
    }

    @Override
    void afterEnvironmentDestroyed() {
        inventoryFile.afterEnvironmentDestroyed()
        tunnelManager.afterEnvironmentDestroyed()
        certificateManager.afterEnvironmentDestroyed()

    }
/**
 * generates certificates for each node if missing
 */
    void generateNodeCertificates() {

        getNodes().each {
            getKeyCertsForNode(it)
        }
    }

    /**
     * Currently known list of nodes
     * @return
     */
    List<ShathelNode> getNodes() {
        return inventoryFile.getNodes()
    }

    /**
     * opens docker tunel and returns docker env vars
     * @param shathelNode
     * @return
     */
    Map<String, String> getDockerEnvironments(ShathelNode shathelNode) {
        return [
                DOCKER_CERT_PATH   : certificateManager.getClientCerts().cert.getParentFile().absolutePath,
                DOCKER_HOST        : "tcp://127.0.0.1:${openDocker(shathelNode)}".toString(),
                DOCKER_TLS_VERIFY  : "1",
                DOCKER_MACHINE_NAME: shathelNode.nodeName,
        ]
    }

    int openTunnel(ShathelNode node, int internalPort) {
        return tunnelManager.openTunnel(sshKeyProvider.keys.privateKey, "${node.publicIp}:22", "${node.privateIp}:${internalPort}")
    }

    private KeyCert getKeyCertsForNode(ShathelNode shathelNode) {
        return certificateManager.getKeyAndCert(shathelNode.nodeName).orElseGet {
            certificateManager.generateKeyAndCert(shathelNode.nodeName, [shathelNode.nodeName, "localhost", shathelNode.publicIp, shathelNode.privateIp, "127.0.0.1"])
        }
    }


    int openDocker(ShathelNode node) {
        return openTunnel(node, 2376)
    }


    String ssh(ShathelNode node, String command) {

    }

    void scp(ShathelNode node, File from, String to) {

    }


}
