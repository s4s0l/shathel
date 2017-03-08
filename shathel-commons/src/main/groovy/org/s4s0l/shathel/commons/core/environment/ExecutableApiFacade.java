package org.s4s0l.shathel.commons.core.environment;

import org.s4s0l.shathel.commons.docker.DockerClientWrapper;
import org.s4s0l.shathel.commons.docker.DockerWrapper;
import org.s4s0l.shathel.commons.secrets.SecretManager;

import java.util.List;
import java.util.Map;

/**
 * @author Matcin Wielgus
 */
public interface ExecutableApiFacade {
    List<String> getNodeNames();

    String getIp(String nodeName);

    String getIpForManagementNode();

    DockerWrapper getDockerForManagementNode();

    String getNameForManagementNode();

    DockerWrapper getDocker(String nodeName);

    void setKernelParam(String param);

    /**
     * returns DOCKER_* environment variables used to talk with
     * docker daemon running on given node
     *
     * @param nodeName node name
     * @return see above
     */
    Map<String, String> getDockerEnvs(String nodeName);

    SecretManager getSecretManager();

    default DockerClientWrapper getClientForManagementNode() {
        String nameForManagementNode = getNameForManagementNode();
        Map<String, String> dockerEnvs = getDockerEnvs(nameForManagementNode);
        return new DockerClientWrapper(dockerEnvs);
    }

}
