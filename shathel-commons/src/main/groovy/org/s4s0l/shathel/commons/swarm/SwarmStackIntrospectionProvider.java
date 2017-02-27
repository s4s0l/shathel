package org.s4s0l.shathel.commons.swarm;

import org.s4s0l.shathel.commons.docker.DockerWrapper;
import org.s4s0l.shathel.commons.localcompose.LocalStackIntrospectionProvider;

import java.util.List;
import java.util.Map;

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
