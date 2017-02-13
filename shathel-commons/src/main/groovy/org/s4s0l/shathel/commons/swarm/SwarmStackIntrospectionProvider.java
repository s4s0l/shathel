package org.s4s0l.shathel.commons.swarm;

import org.s4s0l.shathel.commons.core.environment.StackIntrospection;
import org.s4s0l.shathel.commons.core.environment.StackIntrospectionProvider;
import org.s4s0l.shathel.commons.core.environment.StackIntrospectionResolver;
import org.s4s0l.shathel.commons.core.stack.StackReference;
import org.s4s0l.shathel.commons.docker.DockerWrapper;
import org.s4s0l.shathel.commons.localcompose.LocalStackIntrospectionProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author Matcin Wielgus
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


}
