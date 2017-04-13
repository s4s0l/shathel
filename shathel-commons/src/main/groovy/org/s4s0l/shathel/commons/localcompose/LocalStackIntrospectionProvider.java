package org.s4s0l.shathel.commons.localcompose;

import org.s4s0l.shathel.commons.core.environment.StackIntrospection;
import org.s4s0l.shathel.commons.core.environment.StackIntrospectionProvider;
import org.s4s0l.shathel.commons.core.environment.StackIntrospectionResolver;
import org.s4s0l.shathel.commons.core.stack.StackReference;
import org.s4s0l.shathel.commons.docker.DockerWrapper;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Marcin Wielgus
 */
@Deprecated
public class LocalStackIntrospectionProvider implements StackIntrospectionProvider {

    protected List<Map<String, String>> getByFilter(String filter) {
        return new DockerWrapper().containersLabelsByFilter(filter)
                .stream()
                .filter(x -> x.get("com.docker.compose.service") != null)
                .collect(Collectors.toList());
    }

    protected List<StackIntrospection.Service> getServicesFromOneStackLabels(StackIntrospectionResolver resolver) {
        String deployName = resolver.getShathelLabels().get("org.shathel.stack.deployName");

        return resolver.getLabelValues("com.docker.compose.service")
                .entrySet().stream()
                .map(x -> new StackIntrospection.Service(deployName, x.getKey(), x.getValue().intValue(), x.getValue().intValue(), new HashMap<Integer, Integer>()))
                .collect(Collectors.toList());
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
                Collectors.groupingBy(x -> x.get("org.shathel.stack.ga"),
                        Collectors.mapping(x -> x, Collectors.toList())
                )
        );
        List<StackIntrospection> ret = collect.entrySet().stream()
                .map(x -> getStackIntrospection(x.getValue()))
                .collect(Collectors.toList());
        return new StackIntrospections(ret);

    }
}
