package org.s4s0l.shathel.commons.core.environment;

import org.s4s0l.shathel.commons.cert.CertificateManager;
import org.s4s0l.shathel.commons.docker.DockerClientWrapper;
import org.s4s0l.shathel.commons.docker.DockerWrapper;
import org.s4s0l.shathel.commons.secrets.SecretManager;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author Marcin Wielgus
 */
public interface ExecutableApiFacade {

    List<ShathelNode> getNodes();

    DockerWrapper getDocker(ShathelNode nodeName);

    /**
     * returns DOCKER_* environment variables used to talk with
     * docker daemon running on given node
     *
     * @param nodeName node name
     * @return see above
     */
    Map<String, String> getDockerEnvs(ShathelNode nodeName);


    /**
     * returns host:port under which given port in ingress network will be available
     *
     * @param port the port number
     * @return string of host:port to use to connect to given port on private node network ip
     */
    String openPublishedPort(int port);

    /**
     * Returns available manager node
     *
     * @return first found manager node
     */
    ShathelNode getManagerNode();

    SecretManager getSecretManager();

    CertificateManager getCertificateManager();

    default Map<String, String> getNodeLabels(ShathelNode node) {
        return getManagerNodeWrapper().swarmNodeGetLabels(node.getNodeName());
    }

    default DockerWrapper getManagerNodeWrapper() {
        return getDocker(getManagerNode());
    }

    default DockerClientWrapper getManagerNodeClient() {
        ShathelNode nameForManagementNode = getManagerNode();
        Map<String, String> dockerEnvs = getDockerEnvs(nameForManagementNode);
        return new DockerClientWrapper(dockerEnvs);
    }

}
