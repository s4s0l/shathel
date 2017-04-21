package org.s4s0l.shathel.commons.remoteswarm

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import org.s4s0l.shathel.commons.cert.CertificateManager
import org.s4s0l.shathel.commons.core.environment.ShathelNode
import org.s4s0l.shathel.commons.ssh.SshOperations

/**
 * @author Marcin Wielgus
 */
@TypeChecked
@CompileStatic
interface RemoteEnvironmentAccessManager extends SshOperations {

    void checkPreConditions()

    void afterEnvironmentDestroyed()

    void generateNodeCertificates()

    List<ShathelNode> getNodes()

    Map<String, String> getDockerEnvironments(ShathelNode shathelNode)

    int openTunnel(ShathelNode node, int internalPort)

    CertificateManager getCertificateManager()
}