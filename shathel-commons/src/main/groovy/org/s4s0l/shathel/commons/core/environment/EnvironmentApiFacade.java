package org.s4s0l.shathel.commons.core.environment;

import org.s4s0l.shathel.commons.docker.DockerWrapper;

import java.util.List;
import java.util.Map;

/**
 * @author Matcin Wielgus
 */
public interface EnvironmentApiFacade {
    List<String> getNodeNames();

    String getIp(String nodeName);

    String getIpForManagementNode();

    DockerWrapper getDockerForManagementNode();

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

    int getExpectedNodeCount();

    int getExpectedManagerNodeCount();
}
