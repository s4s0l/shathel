package org.s4s0l.shathel.commons.localcompose;

import org.s4s0l.shathel.commons.core.environment.StackIntrospection;
import org.s4s0l.shathel.commons.core.environment.StackIntrospectionProvider;
import org.s4s0l.shathel.commons.core.environment.StackIntrospectionResolver;
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
        List<Map<String,String>> oneByFilter = new DockerWrapper()
                .containersLabelsByFilter("label=org.shathel.stack.ga=" + reference.getGroup() + ":" + reference.getName());
        if(oneByFilter.isEmpty()){
            return Optional.empty();
        }
        StackIntrospectionResolver resolver = new StackIntrospectionResolver(oneByFilter);
        String o = resolver.getGav();
        return Optional.of(new StackIntrospection(new StackReference(o), resolver.getShathelLabels()));
    }

    @Override
    public List<StackIntrospection> getAllStacks() {
        return null;
    }
}
