package org.s4s0l.shathel.commons.localswarm

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import org.s4s0l.shathel.commons.cert.CertificateManager
import org.s4s0l.shathel.commons.cert.CertificateManagerImpl
import org.s4s0l.shathel.commons.core.ParameterProvider
import org.s4s0l.shathel.commons.core.environment.ExecutableApiFacade
import org.s4s0l.shathel.commons.core.environment.ShathelNode
import org.s4s0l.shathel.commons.docker.DockerWrapper
import org.s4s0l.shathel.commons.secrets.SecretManager
import org.s4s0l.shathel.commons.secrets.SecretManagerApi

/**
 * @author Marcin Wielgus
 */

class LocalSwarmApiFacade implements ExecutableApiFacade {
    private final DockerWrapper dockerWrapper
    private final LocalSwarmEnvironmentContext context
    private ShathelNode shathelNode

    LocalSwarmApiFacade(DockerWrapper dockerWrapper, LocalSwarmEnvironmentContext context) {
        this.dockerWrapper = dockerWrapper
        this.context = context
    }


    @Override
    @TypeChecked
    @CompileStatic
    SecretManagerApi getSecretManager() {
        return new SecretManager(new ParameterProvider() {
            @Override
            Optional<String> getParameter(String name) {
                return context.getEnvironmentParameter(name)
            }
        }, getManagerNodeClient(), context.safeStorage)
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

    @Override
    CertificateManager getCertificateManager() {
        return new CertificateManagerImpl(context.certsDirectory, "TodoRemoveIt".bytes, context.contextName)
    }

}
