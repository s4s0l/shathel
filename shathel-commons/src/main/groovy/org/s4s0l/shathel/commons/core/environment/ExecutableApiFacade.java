package org.s4s0l.shathel.commons.core.environment;

import org.s4s0l.shathel.commons.docker.DockerClientWrapper;
import org.s4s0l.shathel.commons.docker.DockerWrapper;
import org.s4s0l.shathel.commons.secrets.SecretManager;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author Marcin Wielgus
 */
public interface ExecutableApiFacade {

    List<String> getNodeNames();

    String getIp(String nodeName);

    DockerWrapper getDocker(String nodeName);

    /**
     * returns DOCKER_* environment variables used to talk with
     * docker daemon running on given node
     *
     * @param nodeName node name
     * @return see above
     */
    Map<String, String> getDockerEnvs(String nodeName);



    DockerWrapper getDockerForManagementNode();

    String getIpForManagementNode();

    String getNameForManagementNode();

    void setKernelParam(String param);

    Optional<String> getRegistry();

    SecretManager getSecretManager();

    default Map<String, String> getNodeLabels(String nodeName){
        return getDockerForManagementNode().swarmNodeGetLabels(nodeName);
    }

    default DockerClientWrapper getClientForManagementNode() {
        String nameForManagementNode = getNameForManagementNode();
        Map<String, String> dockerEnvs = getDockerEnvs(nameForManagementNode);
        return new DockerClientWrapper(dockerEnvs);
    }

}
