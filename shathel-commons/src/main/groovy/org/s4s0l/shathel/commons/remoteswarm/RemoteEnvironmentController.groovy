package org.s4s0l.shathel.commons.remoteswarm

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import org.s4s0l.shathel.commons.docker.DockerInfoWrapper
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.util.function.Predicate
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
        int mCount = processorContext.getEnvironmentDescription().managersCount
        int wCount = processorContext.getEnvironmentDescription().workersCount
        def nodes = apiFacade.nodes
        return mCount == nodes.collect { it.role == "manager" }.size() &&
                wCount == nodes.collect { it.role == "worker" }.size()
    }

    void initialize() {

        verifyMandarotyParams()

        Map<String, String> envs = createEnvs()

        envs = getImageScript().process(ProcessorCommand.APPLY, envs)
        envs = getInfrastructureScript().process(ProcessorCommand.APPLY, envs)
        accessManager.generateCertificates()
        envs = getSetupScript().process(ProcessorCommand.APPLY, envs)
        envs = getSwarmScript().process(ProcessorCommand.APPLY, envs)

    }

    private RemoteEnvironmentProcessor getImageScript() {
        def imageScript = processors.create(processorContext, processorContext.packageDescription.imagePreparationScript)
        imageScript
    }

    private RemoteEnvironmentProcessor getInfrastructureScript() {
        def script = processors.create(processorContext, processorContext.packageDescription.infrastructureScript)
        script
    }

    private RemoteEnvironmentProcessor getSetupScript() {
        def script = processors.create(processorContext, processorContext.packageDescription.setupScript)
        script
    }

    private RemoteEnvironmentProcessor getSwarmScript() {
        def script = processors.create(processorContext, processorContext.packageDescription.swarmScript)
        script
    }

    private Map<String, String> createEnvs() {
        processorContext.asEnvironmentVariables
    }

    private void verifyMandarotyParams() {
        def variables = processorContext.asEnvironmentVariables
        def mandatory = processorContext.packageDescription.mandatoryEnvs
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
        infrastructureScript.process(ProcessorCommand.START, createEnvs())
    }

    boolean isStarted() {
        def orDefault = infrastructureScript.process(ProcessorCommand.STARTED, createEnvs()).getOrDefault("RESULT", "true")
        return Boolean.parseBoolean(orDefault)
    }

    void verify() {
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
                .map { DockerInfoWrapper x -> new Tuple([x.isSwarmActive(), x.getRemoteManagers()]) }
                .distinct().collect(Collectors.toList())

        if (collect.size() != 1 ||
                ((Map) collect.get(0).get(1)).size() != machines.stream().filter { DockerInfoWrapper x -> x.isManager() }.count()) {
            throw new RuntimeException("Inconsistent swarm cluster detected")
        }
    }

    void destroy() {
        infrastructureScript.process(ProcessorCommand.DESTROY, createEnvs())
    }

    void stop() {
        infrastructureScript.process(ProcessorCommand.STOP, createEnvs())
    }
}
