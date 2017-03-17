package org.s4s0l.shathel.commons.swarm;

import org.s4s0l.shathel.commons.core.environment.EnricherExecutable;
import org.s4s0l.shathel.commons.core.environment.EnricherExecutableParams;
import org.s4s0l.shathel.commons.core.environment.ExecutableApiFacade;
import org.s4s0l.shathel.commons.core.model.ComposeFileModel;
import org.s4s0l.shathel.commons.core.stack.StackDescription;
import org.s4s0l.shathel.commons.docker.DockerWrapper;
import org.s4s0l.shathel.commons.utils.TemplateUtils;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * @author Marcin Wielgus
 */
public class SwarmBuildingEnricher extends EnricherExecutable {
    private final Optional<String> repository;

    public SwarmBuildingEnricher(Optional<String> repository) {
        this.repository = repository;
    }

    @Override
    protected void execute(EnricherExecutableParams paramz) {
        ExecutableApiFacade apiFacade = paramz.getApiFacade();
        ComposeFileModel model = paramz.getModel();
        StackDescription stack = paramz.getStack();
        EnricherExecutableParams.Provisioners provisioners = paramz.getProvisioners();
        Map<String, String> environment = paramz.getEnvironment();
        DockerWrapper dockerForManagementNode = apiFacade.getDockerForManagementNode();
        model.mapBuilds((service, params) -> {
            String dockerfile = TemplateUtils.fillEnvironmentVariables((String) params.get("dockerfile"), environment);
            String context = TemplateUtils.fillEnvironmentVariables((String) params.get("context"), environment);
            Map<String, String> args = new HashMap<>();

            for (Map.Entry<String, String> e : ((Map<String, String>) params.get("args")).entrySet()) {
                args.put(
                        TemplateUtils.fillEnvironmentVariables(e.getKey(), environment),
                        TemplateUtils.fillEnvironmentVariables(e.getValue(), environment)
                );
            }

            String imageName = (stack.getReference().getName() + "." + service).toLowerCase().replaceAll("[^a-z0-9]", ".");
            String repoPrefix = repository.isPresent() ? repository.get() + "/" : "";
            String tag = repoPrefix + imageName + ":" + stack.getReference().getVersion();
            File contextDir = new File(stack.getStackResources().getComposeFileDirectory(), context);
            provisioners.add("build-and-tag:" + contextDir.getAbsolutePath(), executionContext -> {
                dockerForManagementNode.buildAndTag(
                        contextDir,
                        dockerfile, args, tag);
                if (repository.isPresent()) {
                    dockerForManagementNode.push(tag);
                }
            });

            return tag;

        });

    }


}
