package org.s4s0l.shathel.commons.swarm;

import org.s4s0l.shathel.commons.core.environment.StackIntrospection;
import org.s4s0l.shathel.commons.core.environment.StackIntrospectionResolver;
import org.s4s0l.shathel.commons.docker.DockerWrapper;
import org.s4s0l.shathel.commons.localcompose.LocalStackIntrospectionProvider;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Marcin Wielgus
 */
public class SwarmStackIntrospectionProvider extends LocalStackIntrospectionProvider {
    private final DockerWrapper docker;

    public SwarmStackIntrospectionProvider(DockerWrapper docker) {
        this.docker = docker;
    }

    @Override
    protected List<Map<String, String>> getByFilter(String filter) {
        return docker.servicesOfContainersMatching(filter);
    }

    @Override
    protected List<StackIntrospection.Service> getServicesFromOneStackLabels(StackIntrospectionResolver resolver) {
        return resolver.getMap().stream().map(x -> new StackIntrospection.Service(
                x.get("shathel.service.name"),
                Integer.parseInt(x.get("shathel.service.expectedCount")),
                Integer.parseInt(x.get("shathel.service.count")

                ))).collect(Collectors.toList());
    }

}
