package org.s4s0l.shathel.commons.swarm;

import org.s4s0l.shathel.commons.core.environment.EnricherExecutable;
import org.s4s0l.shathel.commons.core.environment.EnricherExecutableParams;
import org.s4s0l.shathel.commons.core.environment.EnvironmentContext;
import org.s4s0l.shathel.commons.core.environment.ExecutableApiFacade;
import org.s4s0l.shathel.commons.core.model.ComposeFileModel;
import org.s4s0l.shathel.commons.core.stack.StackDescription;
import org.s4s0l.shathel.commons.docker.DockerWrapper;
import org.s4s0l.shathel.commons.scripts.Executable;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Marcin Wielgus
 */
public class SwarmBuildingEnricher extends EnricherExecutable {
    private final String repository;

    public SwarmBuildingEnricher(String repository) {
        this.repository = repository;
    }

    @Override
    protected List<Executable> executeProvidingProvisioner(EnricherExecutableParams paramz) {
        ExecutableApiFacade apiFacade = paramz.getApiFacade();
        ComposeFileModel model = paramz.getModel();
        StackDescription stack = paramz.getStack();
        DockerWrapper dockerForManagementNode = apiFacade.getDockerForManagementNode();
        List<Executable> execs = new ArrayList<>();
        model.mapBuilds((service, params) -> {
            String dockerfile = (String) params.get("dockerfile");
            String context = (String) params.get("context");
            Map<String, String> args = (Map<String, String>) params.get("args");
            String imageName = (stack.getReference().getName() + "." + service).toLowerCase().replaceAll("[^a-z0-9]", ".");
            String repoPrefix = repository == null ? "" : repository + "/";
            String tag = repoPrefix + imageName + ":" + stack.getReference().getVersion();
            execs.add(executionContext -> {
                dockerForManagementNode.buildAndPush(
                        new File(stack.getStackResources().getComposeFileDirectory(), context),
                        dockerfile, args, tag);
                return "ok";
            });

            return tag;

        });
        return execs;
    }


}
