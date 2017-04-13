package org.s4s0l.shathel.commons.remoteswarm

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import org.s4s0l.shathel.commons.core.environment.ShathelNode

/**
 * @author Marcin Wielgus
 */
@TypeChecked
@CompileStatic
interface RemoteEnvironmentAccessManager {

    void generateCertificates()

    List<ShathelNode> getNodes()

    Map<String, String> getDockerEnvironments(ShathelNode shathelNode)

    int openTunnel(ShathelNode node, int internalPort)

    String ssh(ShathelNode node, String command)

    void scp(ShathelNode node, File from, String to)

}