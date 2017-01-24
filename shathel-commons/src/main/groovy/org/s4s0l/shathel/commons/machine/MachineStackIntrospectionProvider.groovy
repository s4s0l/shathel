package org.s4s0l.shathel.commons.machine

import org.s4s0l.shathel.commons.core.environment.StackIntrospection
import org.s4s0l.shathel.commons.core.environment.StackIntrospectionProvider
import org.s4s0l.shathel.commons.core.environment.StackIntrospectionResolver
import org.s4s0l.shathel.commons.core.stack.StackReference
import org.s4s0l.shathel.commons.docker.DockerWrapper

/**
 * @author Matcin Wielgus
 */
class MachineStackIntrospectionProvider implements StackIntrospectionProvider {
    private final DockerMachineCommons docker;

    MachineStackIntrospectionProvider(DockerMachineCommons docker) {
        this.docker = docker
    }

    @Override
    Optional<StackIntrospection> getIntrospection(StackReference reference) {
        def node = docker.getDockerWrapperForManagementNode()
        List<Map<String,String>> matching = node.getServicesOfContainersMatching("label=org.shathel.stack.ga=" + reference.getGroup() + ":" + reference.getName())
        if(matching.isEmpty()){
            return Optional.empty()
        }
        StackIntrospectionResolver resolver = new StackIntrospectionResolver(matching);
        String o = resolver.getGav();
        return Optional.of(new StackIntrospection(new StackReference(o), resolver.getShathelLabels()));
    }

    @Override
    List<StackIntrospection> getAllStacks() {
        return []
    }
}
