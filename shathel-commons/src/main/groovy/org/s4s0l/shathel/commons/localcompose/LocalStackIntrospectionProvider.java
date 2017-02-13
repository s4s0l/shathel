package org.s4s0l.shathel.commons.localcompose;

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
 * @author Matcin Wielgus
 */
public class LocalStackIntrospectionProvider implements StackIntrospectionProvider {

    protected List<Map<String, String>> getByFilter(String filter) {
        return new DockerWrapper()
                .containersLabelsByFilter(filter);
    }

    @Override
    public Optional<StackIntrospection> getIntrospection(StackReference reference) {
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
        return new StackIntrospection(new StackReference(o), resolver.getShathelLabels());
    }

    @Override
    public List<StackIntrospection> getAllStacks() {
        List<Map<String, String>> oneByFilter = getByFilter("label=org.shathel.stack.marker=true");
        Map<String, List<Map<String, String>>> collect = oneByFilter.stream().collect(
                Collectors.groupingBy(x -> x.get("org.shathel.stack.ga"),
                        Collectors.mapping(x -> x, Collectors.toList())
                )
        );
        List<StackIntrospection> ret=  collect.entrySet().stream()
                .map(x -> getStackIntrospection(x.getValue()))
                .collect(Collectors.toList());
        return ret;

    }
}
