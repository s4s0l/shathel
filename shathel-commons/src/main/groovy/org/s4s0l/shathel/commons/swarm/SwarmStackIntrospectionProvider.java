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
        return docker.servicesLabels(filter);
    }

    @Override
    protected List<StackIntrospection.Service> getServicesFromOneStackLabels(StackIntrospectionResolver resolver) {
        return resolver.getMap().stream().map(x -> new StackIntrospection.Service(
                resolver.getShathelLabels().get("org.shathel.stack.deployName"),
                x.get("shathel.service.name"),
                Integer.parseInt(x.get("shathel.service.count")),
                Integer.parseInt(x.get("shathel.service.expectedCount")),
                getPortMapping(x)
        )).collect(Collectors.toList());
    }

    public static final String SHATHEL_SERVICE_PORT_LABEL_PREFIX = "shathel.service.port.";

    private Map<Integer, Integer> getPortMapping(Map<String, String> labelsMap) {
        return labelsMap.entrySet().stream()
                .filter(it -> it.getKey().startsWith(SHATHEL_SERVICE_PORT_LABEL_PREFIX))
                .collect(Collectors.toMap(
                        it -> Integer.parseInt(it.getKey().substring(SHATHEL_SERVICE_PORT_LABEL_PREFIX.length())),
                        it -> Integer.parseInt(it.getValue()))
                );

    }

}
