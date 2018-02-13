package org.s4s0l.shathel.commons.remoteswarm

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import org.s4s0l.shathel.commons.docker.DockerInfoWrapper
import org.s4s0l.shathel.commons.utils.ExecutableResults
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.util.stream.Collectors

/**
 * @author Marcin Wielgus
 */
@TypeChecked
@CompileStatic
class RemoteEnvironmentController {
    private static
    final Logger LOGGER = LoggerFactory.getLogger(RemoteEnvironmentController.class)
    final RemoteEnvironmentPackageContext processorContext
    final RemoteEnvironmentProcessorsFactory processors
    final RemoteEnvironmentApiFacade apiFacade
    final RemoteEnvironmentAccessManager accessManager

    RemoteEnvironmentController(RemoteEnvironmentPackageContext processorContext, RemoteEnvironmentProcessorsFactory processors, RemoteEnvironmentApiFacade apiFacade, RemoteEnvironmentAccessManager accessManager) {
        this.processorContext = processorContext
        this.processors = processors
        this.apiFacade = apiFacade
        this.accessManager = accessManager
    }

    boolean isInitialized() {
        verifyMandatoryParams()
        accessManager.checkPreConditions()
        int mCount = processorContext.managersCount
        int wCount = processorContext.workersCount
        def nodes = apiFacade.nodes
        return mCount == nodes.findAll { it.role == "manager" }.size() &&
                wCount == nodes.findAll { it.role == "worker" }.size() &&
                inited()
    }

    void initialize() {
        verifyMandatoryParams()
        accessManager.checkPreConditions()

        Map<String, String> envs = createEnvs()

        ExecutableResults res = getImageScript().process(ProcessorCommand.APPLY, envs)
        res = getInfrastructureScript().process(ProcessorCommand.APPLY, envs)
        accessManager.generateNodeCertificates()
        res = getSetupScript().process(ProcessorCommand.APPLY, envs)
        res = getSwarmScript().process(ProcessorCommand.APPLY, envs)

    }

    private RemoteEnvironmentProcessor getImageScript() {
        def imageScript = processors.create(processorContext.imagePreparationScript)
        imageScript
    }

    private RemoteEnvironmentProcessor getInfrastructureScript() {
        def script = processors.create(processorContext.infrastructureScript)
        script
    }

    private RemoteEnvironmentProcessor getSetupScript() {
        def script = processors.create(processorContext.setupScript)
        script
    }

    private RemoteEnvironmentProcessor getSwarmScript() {
        def script = processors.create(processorContext.swarmScript)
        script
    }

    private Map<String, String> createEnvs() {
        processorContext.asEnvironmentVariables
    }

    private void verifyMandatoryParams() {
        def variables = createEnvs()
        def mandatory = processorContext.mandatoryEnvs
        def missingMessage = mandatory.findAll {
            variables.get(it.key) == null
        }.collect {
            "* ${it.key}, should contain: ${it.value}."
        }.join("\n")
        if ("" != missingMessage) {
            throw new RuntimeException("Missing variables:\n$missingMessage")
        }
    }

    void start() {
        verifyMandatoryParams()
        accessManager.checkPreConditions()
        infrastructureScript.process(ProcessorCommand.START, createEnvs())
    }

    boolean inited() {
        verifyMandatoryParams()
        accessManager.checkPreConditions()
        Map<String, String> envs = createEnvs()
        ExecutableResults res = getImageScript().process(ProcessorCommand.APPLY, envs)
        return getInfrastructureScript().process(ProcessorCommand.INITED, envs).status
    }

    boolean isStarted() {
        verifyMandatoryParams()
        accessManager.checkPreConditions()
        Map<String, String> envs = createEnvs()
        ExecutableResults res = getImageScript().process(ProcessorCommand.APPLY, envs)
        return getInfrastructureScript().process(ProcessorCommand.STARTED, envs).status
    }

    void verify() {
        verifyMandatoryParams()
        accessManager.checkPreConditions()
        def nodes = apiFacade.nodes

        List<DockerInfoWrapper> machines = nodes.collect {
            new DockerInfoWrapper(apiFacade.getDocker(it).daemonInfo(), it.nodeName)
        }

        //is there at least one manager
        machines.stream()
                .filter { DockerInfoWrapper x -> return x.isManager() && x.isSwarmActive() }
                .findFirst().orElseThrow { new RuntimeException("No Manager found") }

        //every machine must have swarm enabled
        machines.stream()
                .filter { DockerInfoWrapper x -> !x.isSwarmActive() }
                .forEach { DockerInfoWrapper x -> throw new RuntimeException("All nodes must be swarm enabled") }

        //all must belong to same cluster and have same managers visibility
        List<Tuple> collect = machines.stream()
                .map { DockerInfoWrapper x -> new Tuple([x.isSwarmActive(), x.getRemoteManagers()] as Object[]) }
                .distinct().collect(Collectors.toList())

        if (collect.size() != 1 ||
                ((Map) collect.get(0).get(1)).size() != machines.stream().filter { DockerInfoWrapper x -> x.isManager() }.count()) {
            throw new RuntimeException("Inconsistent swarm cluster detected")
        }
    }

    void destroy() {
        accessManager.checkPreConditions()
        accessManager.closeAllTunnels()
        infrastructureScript.process(ProcessorCommand.DESTROY, createEnvs())
        accessManager.afterEnvironmentDestroyed()

    }

    void stop() {
        accessManager.checkPreConditions()
        accessManager.closeAllTunnels()
        infrastructureScript.process(ProcessorCommand.STOP, createEnvs())
    }
}
