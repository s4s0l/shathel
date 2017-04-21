package org.s4s0l.shathel.commons.localswarm

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import org.s4s0l.shathel.commons.core.environment.EnvironmentContext
import org.s4s0l.shathel.commons.core.environment.ExecutableApiFacade
import org.s4s0l.shathel.commons.core.environment.ShathelNode
import org.s4s0l.shathel.commons.docker.DockerWrapper
import org.s4s0l.shathel.commons.secrets.SecretManager
import org.slf4j.Logger

import static org.slf4j.LoggerFactory.getLogger

/**
 * @author Marcin Wielgus
 */

class LocalSwarmApiFacade implements ExecutableApiFacade {
    private static final Logger LOGGER = getLogger(LocalSwarmApiFacade.class)
    private final DockerWrapper dockerWrapper
    private final EnvironmentContext context
    private ShathelNode shathelNode

    LocalSwarmApiFacade(DockerWrapper dockerWrapper, EnvironmentContext context) {
        this.dockerWrapper = dockerWrapper
        this.context = context
    }


    @Override
    @TypeChecked
    @CompileStatic
    SecretManager getSecretManager() {
        return new SecretManager(context.getEnvironmentDescription(), getManagerNodeClient())
    }


    @Override
    List<ShathelNode> getNodes() {
        return Collections.singletonList(getManagerNode())
    }

    @Override
    DockerWrapper getDocker(ShathelNode nodeName) {
        if (nodeName.nodeName == getManagerNode().nodeName) {
            return dockerWrapper
        } else {
            throw new RuntimeException("Unable to create docker wrapper for node " + nodeName)
        }
    }

    @Override
    Map<String, String> getDockerEnvs(ShathelNode nodeName) {
        return Collections.emptyMap()
    }

    @Override
    String openPublishedPort(int port) {
        return "127.0.0.1:" + port
    }


    @Override
    synchronized ShathelNode getManagerNode() {
        if (shathelNode == null) {
            def currentNodeId = dockerWrapper.daemonInfo().Swarm.NodeID
            def matching = dockerWrapper.swarmNodes().values().findAll {
                it.id == currentNodeId
            }.collect {
                new ShathelNode(it.hostName, it.hostName, "127.0.0.1", "manager")
            }
            if (matching.size() == 1) {
                shathelNode = matching.head()
            } else {
                throw new RuntimeException("There is sth wrong, multiple nodes have same ID?")
            }
        }
        return shathelNode
    }

}
