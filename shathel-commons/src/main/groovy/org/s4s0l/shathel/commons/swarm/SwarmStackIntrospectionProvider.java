package org.s4s0l.shathel.commons.swarm;

import org.s4s0l.shathel.commons.core.environment.StackIntrospection;
import org.s4s0l.shathel.commons.core.environment.StackIntrospectionProvider;
import org.s4s0l.shathel.commons.core.environment.StackIntrospectionResolver;
import org.s4s0l.shathel.commons.core.stack.StackReference;
import org.s4s0l.shathel.commons.docker.DockerWrapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author Matcin Wielgus
 */
public class SwarmStackIntrospectionProvider implements StackIntrospectionProvider {
    public SwarmStackIntrospectionProvider(DockerWrapper docker) {
        this.docker = docker;
    }

    @Override
    public Optional<StackIntrospection> getIntrospection(StackReference reference) {
        List<Map<String, String>> matching = docker.servicesOfContainersMatching("label=org.shathel.stack.ga=" + reference.getGroup() + ":" + reference.getName());
        if (matching.isEmpty()) {
            return Optional.empty();
        }

        StackIntrospectionResolver resolver = new StackIntrospectionResolver(matching);
        String o = resolver.getGav();
        return Optional.of(new StackIntrospection(new StackReference(o), resolver.getShathelLabels()));
    }

    @Override
    public List<StackIntrospection> getAllStacks() {
        return new ArrayList();
    }

    private final DockerWrapper docker;
}
