package org.s4s0l.shathel.commons.swarm;

import org.s4s0l.shathel.commons.core.DefaultGlobalEnricherProvider;
import org.s4s0l.shathel.commons.core.environment.StackIntrospection;
import org.s4s0l.shathel.commons.core.environment.StackIntrospectionProvider;
import org.s4s0l.shathel.commons.core.environment.StackIntrospectionResolver;
import org.s4s0l.shathel.commons.core.stack.StackReference;
import org.s4s0l.shathel.commons.docker.DockerWrapper;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author Marcin Wielgus
 */
public class SwarmStackIntrospectionProvider implements StackIntrospectionProvider  {
    private final DockerWrapper docker;

    public SwarmStackIntrospectionProvider(DockerWrapper docker) {
        this.docker = docker;
    }

    protected List<Map<String, String>> getByFilter(String filter) {
        return docker.servicesLabels(filter);
    }

    protected List<StackIntrospection.Service> getServicesFromOneStackLabels(StackIntrospectionResolver resolver) {
        return resolver.getMap().stream().map(x -> new StackIntrospection.Service(
                resolver.getShathelLabels().get(DefaultGlobalEnricherProvider.LABEL_SHATHEL_STACK_DEPLOY_NAME),
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


    @Override
    public Optional<StackIntrospection> getStackIntrospection(StackReference reference) {
        List<Map<String, String>> oneByFilter = getByFilter("label=org.shathel.stack.ga=" + reference.getGroup() + ":" + reference.getName());
        if (oneByFilter.isEmpty()) {
            return Optional.empty();
        }
        StackIntrospection value = getStackIntrospection(oneByFilter);
        return Optional.of(value);
    }

    private StackIntrospection getStackIntrospection(List<Map<String, String>> oneByFilter) {
        StackIntrospectionResolver resolver = new StackIntrospectionResolver(oneByFilter);
        String o = resolver.getGav();
        List<StackIntrospection.Service> services = getServicesFromOneStackLabels(resolver);

        return new StackIntrospection(new StackReference(o), services, resolver.getShathelLabels());
    }


    @Override
    public StackIntrospections getAllStacks() {
        List<Map<String, String>> oneByFilter = getByFilter("label=org.shathel.stack.marker=true");
        Map<String, List<Map<String, String>>> collect = oneByFilter.stream().collect(
                Collectors.groupingBy(x -> x.get(DefaultGlobalEnricherProvider.LABEL_SHATHEL_STACK_GA),
                        Collectors.mapping(x -> x, Collectors.toList())
                )
        );
        List<StackIntrospection> ret = collect.entrySet().stream()
                .map(x -> getStackIntrospection(x.getValue()))
                .collect(Collectors.toList());
        return new StackIntrospections(ret);

    }

}
