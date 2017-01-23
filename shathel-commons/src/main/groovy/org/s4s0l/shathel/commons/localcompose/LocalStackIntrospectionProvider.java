package org.s4s0l.shathel.commons.localcompose;

import org.s4s0l.shathel.commons.core.environment.StackIntrospection;
import org.s4s0l.shathel.commons.core.environment.StackIntrospectionProvider;
import org.s4s0l.shathel.commons.core.stack.StackReference;
import org.s4s0l.shathel.commons.docker.DockerWrapper;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author Matcin Wielgus
 */
public class LocalStackIntrospectionProvider implements StackIntrospectionProvider {

    @Override
    public Optional<StackIntrospection> getIntrospection(StackReference reference) {
        Map oneByFilter = new DockerWrapper().getLabelsOfOneByFilter("label=org.shathel.stack.ga=" + reference.getGroup() + ":" + reference.getName());
        if (oneByFilter.get("org.shathel.stack.ga") == null) {
            return Optional.empty();
        }
        String o = (String) oneByFilter.get("org.shathel.stack.gav");
        return Optional.of(new StackIntrospection(new StackReference(o), oneByFilter));
    }

    @Override
    public List<StackIntrospection> getAllStacks() {
        return null;
    }
}
