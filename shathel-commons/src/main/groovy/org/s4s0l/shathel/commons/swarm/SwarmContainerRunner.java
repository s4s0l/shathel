package org.s4s0l.shathel.commons.swarm;

import org.s4s0l.shathel.commons.core.DockerLoginInfo;
import org.s4s0l.shathel.commons.core.environment.EnvironmentContainerRunner;
import org.s4s0l.shathel.commons.core.environment.EnvironmentContainerRunnerContext;
import org.s4s0l.shathel.commons.core.environment.StackCommand;
import org.s4s0l.shathel.commons.core.stack.StackReference;
import org.s4s0l.shathel.commons.docker.DockerWrapper;
import org.slf4j.Logger;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * @author Marcin Wielgus
 */
public class SwarmContainerRunner implements EnvironmentContainerRunner, EnvironmentContainerRunnerContext {
    private static final int RUNNING_CHECK_TRIES = 120;
    private static final int RUNNING_CHECK_INTERVAL_SECS = 1;
    private static final Logger LOGGER = getLogger(SwarmContainerRunner.class);
    private final Optional<DockerLoginInfo> dockerLogin;

    public SwarmContainerRunner(DockerWrapper docker, Optional<DockerLoginInfo> dockerLogin) {
        this.docker = docker;
        this.dockerLogin = dockerLogin;
    }

    @Override
    public EnvironmentContainerRunnerContext createContext() {
        return this;
    }

    @Override
    public void startContainers(StackCommand command, File composeFile) {
        LOGGER.info("Running: {} ({}).", command.getDescription().getGav(), command.getType().toString());
        dockerLogin.ifPresent(docker::login);
        docker.stackDeploy(composeFile, command.getDescription().getDeployName(), command.getEnvironment());

        //waiting for startup
        StackReference reference = command.getDescription().getReference();
        waitForServicesToBeComplete(reference);
        if (command.getType() == StackCommand.Type.UPDATE) {
            waitForUpdateToFinish(reference);
        }
        return;


    }

    private void waitForUpdateToFinish(StackReference reference) {
        List<Map<String, String>> maps = docker.servicesStatus("label=org.shathel.stack.ga=" + reference.getGroup() + ":" + reference.getName());
        List<String> serviceNames = maps.stream().map(x -> x.get("name")).collect(Collectors.toList());

        services:
        for (String serviceName : serviceNames) {
            int atts = 0;
            while (atts < RUNNING_CHECK_TRIES) {
                String status = "updating";
                Map map = docker.serviceInspect(serviceName);
                Map updateStatus = (Map) map.get("UpdateStatus");
                if (updateStatus == null) {
                    //this happens when service is removed?!?
                    //todo: rethink how to handle - remove by hand?
                    //for now lets just ignore it, new behaviour from 17.04
                    //before it was left untouched
                    continue services;
                }
                status = (String) updateStatus.get("State");
                LOGGER.info("Running: {}. Service {} has update status {}, waiting...", reference.getGav(), serviceName, status);
                if (null == status || "completed".equals(status)) {
                    continue services;
                }
                sleepAWhile();
                atts++;
            }
            throw new RuntimeException("Stack has some troubles to update! (" + reference + ")");
        }


    }

    private void waitForServicesToBeComplete(StackReference reference) {
        int atts = 0;
        while (atts < RUNNING_CHECK_TRIES) {
            List<Map<String, String>> maps = docker.servicesStatus("label=org.shathel.stack.ga=" + reference.getGroup() + ":" + reference.getName());
            Optional<Map<String, String>> notStarted = maps.stream()
                    .filter(x -> !"1".equals(x.get("ratio")))
                    .findAny()
                    .map(x -> logNotStartedService(reference, x));
            if (!notStarted.isPresent()) {
                return;
            }
            sleepAWhile();
            atts++;
        }
        throw new RuntimeException("Stack has some troubles to start! (" + reference + ")");
    }

    private void sleepAWhile() {
        try {
            Thread.sleep(RUNNING_CHECK_INTERVAL_SECS * 1000);
        } catch (InterruptedException e) {
            throw new RuntimeException("Got interrupted. ", e);
        }
    }

    private Map<String, String> logNotStartedService(StackReference ref, Map<String, String> x) {
        LOGGER.info("Running: {}. Service {} is started in {}%, waiting...", ref.getGav(), x.get("name"), 100 * Float.parseFloat(x.get("ratio")));
        return x;
    }

    @Override
    public void stopContainers(StackCommand command, File composeFile) {
        LOGGER.info("Stopping: {}.", command.getDescription().getGav());
        docker.stackUnDeploy(composeFile, command.getDescription().getDeployName(), command.getEnvironment());
    }

    @Override
    public void close() throws Exception {

    }

    private final DockerWrapper docker;
}
