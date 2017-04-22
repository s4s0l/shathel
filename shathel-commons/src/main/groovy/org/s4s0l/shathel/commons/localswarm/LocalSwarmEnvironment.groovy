package org.s4s0l.shathel.commons.localswarm

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import org.apache.commons.lang.StringUtils
import org.codehaus.groovy.runtime.ResourceGroovyMethods
import org.s4s0l.shathel.commons.core.environment.*
import org.s4s0l.shathel.commons.docker.DockerWrapper
import org.s4s0l.shathel.commons.scripts.NamedExecutable
import org.s4s0l.shathel.commons.scripts.ansible.AnsibleScriptContext
import org.s4s0l.shathel.commons.secrets.SecretsEnricher
import org.s4s0l.shathel.commons.swarm.BuildingEnricher
import org.s4s0l.shathel.commons.swarm.MandatoryEnvironmentsValidator
import org.s4s0l.shathel.commons.swarm.SwarmContainerRunner
import org.s4s0l.shathel.commons.swarm.SwarmStackIntrospectionProvider
import org.slf4j.Logger

import java.io.IOException
import java.util.Arrays
import java.util.List

import static org.slf4j.LoggerFactory.getLogger

/**
 * @author Marcin Wielgus
 */
@TypeChecked
@CompileStatic
class LocalSwarmEnvironment implements Environment {
    private static final Logger LOGGER = getLogger(LocalSwarmEnvironment.class)
    private final LocalSwarmEnvironmentContext context
    private final DockerWrapper dockerWrapper = new DockerWrapper()
    private final LocalSwarmApiFacade apiFacade

    LocalSwarmEnvironment(LocalSwarmEnvironmentContext context) {
        this.context = context
        this.apiFacade = new LocalSwarmApiFacade(getDockerWrapper(), context)
    }

    @Override
    boolean isInitialized() {
        getDockerWrapper().swarmActive() && context.getAnsibleInventoryFile().exists()
    }

    @Override
    void initialize() {
        if (!getDockerWrapper().swarmActive()) {
            getDockerWrapper().swarmInit()
        }

        ShathelNode managerNode = getEnvironmentApiFacade().getManagerNode()

        try {
            ResourceGroovyMethods.setText(context.getAnsibleInventoryFile(),
                    new StringBuilder()
                            .append("[shathel_manager_hosts]")
                            .append("\n")
                            .append("127.0.0.1")
                            .append(" private_ip=")
                            .append(managerNode.getPrivateIp())
                            .append(" public_ip=")
                            .append(managerNode.getPublicIp())
                            .append(" shathel_name=")
                            .append(managerNode.getNodeName())
                            .append(" shathel_role=manager\n[shathel_worker_hosts]\n\n")
                            .toString()
            )
        } catch (IOException e) {
            throw new RuntimeException("Unable to create ansible inventory!")
        }

    }

    private DockerWrapper getDockerWrapper() {
        return dockerWrapper
    }

    @Override
    void start() {

    }

    @Override
    boolean isStarted() {
        return true
    }

    @Override
    void stop() {

    }

    @Override
    void destroy() {

    }

    @Override
    void verify() {
        if (!StringUtils.isEmpty(System.getenv("DOCKER_HOST"))) {
            throw new RuntimeException("DOCKER_HOST env var is not empty, not allowed in local swarm environment")
        }
        if (!isInitialized()) {
            throw new RuntimeException("Not initialized. Local docker is not in swarm mode.")
        }
    }

    @Override
    void save() {

    }

    @Override
    void load() {

    }

    @Override
    StackIntrospectionProvider getIntrospectionProvider() {
        return new SwarmStackIntrospectionProvider(getDockerWrapper())
    }

    @Override
    EnvironmentContainerRunner getContainerRunner() {
        return new SwarmContainerRunner(getDockerWrapper())
    }

    @Override
    EnvironmentContext getEnvironmentContext() {
        return context
    }

    @Override
    LocalSwarmApiFacade getEnvironmentApiFacade() {
        return apiFacade
    }

    @Override
    List<NamedExecutable> getEnvironmentEnrichers() {
        return Arrays.<NamedExecutable> asList(
                new LocalMountingEnricher(),
                new BuildingEnricher(),
                new SecretsEnricher(),
                new MandatoryEnvironmentsValidator()
        )
    }

    @Override
    Optional<AnsibleScriptContext> getAnsibleScriptContext() {
        boolean ansibleEnabled = context.getEnvironmentDescription().getParameterAsBoolean("ansible_enabled").orElse(false)
        return Optional.ofNullable(
                ansibleEnabled ?
                        new AnsibleScriptContext(context.getRemoteUser(),
                                "SHATHEL_ENV_ANSIBLE_BECOME_PASSWORD",
                                context.getAnsibleInventoryFile())
                        : null)

    }
}
