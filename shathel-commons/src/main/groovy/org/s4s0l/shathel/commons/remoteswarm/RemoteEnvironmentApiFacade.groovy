package org.s4s0l.shathel.commons.remoteswarm

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import org.s4s0l.shathel.commons.cert.CertificateManager
import org.s4s0l.shathel.commons.core.environment.ExecutableApiFacade
import org.s4s0l.shathel.commons.core.environment.ShathelNode
import org.s4s0l.shathel.commons.docker.DockerWrapper
import org.s4s0l.shathel.commons.secrets.SecretManager
import org.s4s0l.shathel.commons.ssh.SshOperations
import org.s4s0l.shathel.commons.utils.ExecWrapper
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * @author Marcin Wielgus
 */
@TypeChecked
@CompileStatic
class RemoteEnvironmentApiFacade implements ExecutableApiFacade {
    private static
    final Logger LOGGER = LoggerFactory.getLogger(RemoteEnvironmentApiFacade.class)

    private final RemoteEnvironmentAccessManager accessManager
    private final RemoteEnvironmentPackageContext packageContext

    RemoteEnvironmentApiFacade(RemoteEnvironmentAccessManager accessManager, RemoteEnvironmentPackageContext packageContext) {
        this.accessManager = accessManager
        this.packageContext = packageContext
    }

    @Override
    List<ShathelNode> getNodes() {
        return accessManager.getNodes()
    }

    @Override
    DockerWrapper getDocker(ShathelNode nodeName) {
        new DockerWrapper(new ExecWrapper(LOGGER, "docker", getDockerEnvs(nodeName)))
    }

    @Override
    Map<String, String> getDockerEnvs(ShathelNode nodeName) {
        return accessManager.getDockerEnvironments(nodeName)
    }

    @Override
    String openPublishedPort(int port) {
        return "127.0.0.1:${accessManager.openTunnel(getManagerNode(), port)}"
    }

    @Override
    ShathelNode getManagerNode() {
        //TODO: check if is accessible!
        return getNodes().stream().filter { it.role == "manager" }
                .findFirst().orElseThrow {
            new RuntimeException("Unable to find suitable manager node")
        }
    }


    @Override
    SecretManager getSecretManager() {
        return new SecretManager(packageContext.getEnvironmentDescription(), getManagerNodeClient())
    }

    SshOperations getSshOperaions() {
        return accessManager
    }

    @Override
    CertificateManager getCertificateManager() {
        return accessManager.getCertificateManager()
    }


//    void setKernelParam(String param) {
//        getNodeNames().each { node ->
//            getWrapper().sudo(node, "sysctl -w $param")
//            String cont = getWrapper().sudo(node, "cat /etc/sysctl.conf")
//            def name = param.split("=")[0]
//            if (cont.contains("${name}=")) {
//                getWrapper().sudo(node, "sed -i.bak s/${name}=.*/$param/g /etc/sysctl.conf")
//            } else {
//                getWrapper().exec.executeForOutput(null, new File("."), [:], "ssh", node, "sudo", "/bin/sh", "-c",
//                        "\"echo '$param' >> /etc/sysctl.conf\"")
//            }
//            cont = getWrapper().sudo(node, "cat /var/lib/boot2docker/profile")
//            if (cont.contains("sysctl -w ${name}=")) {
//                getWrapper().exec.executeForOutput(null, new File("."), [:], "ssh", node, "sudo", "/bin/sh", "-c",
//                        "\"sed -i.bak s/sysctl\\ -w\\ ${name}=.*/sysctl\\ -w\\ $param/g /var/lib/boot2docker/profile\"")
//            } else {
//                getWrapper().exec.executeForOutput(null, new File("."), [:], "ssh", node, "sudo", "/bin/sh", "-c",
//                        "\"echo 'sysctl -w $param' >> /var/lib/boot2docker/profile\"")
//            }
//
//        }
//    }

}
