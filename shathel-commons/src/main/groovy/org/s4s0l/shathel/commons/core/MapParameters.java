package org.s4s0l.shathel.commons.core;

import lombok.Builder;
import lombok.Singular;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * @author Marcin Wielgus
 */
@Builder
public class MapParameters implements Parameters {
    @Singular
    private final Map<String, String> parameters;

    public Optional<String> getParameter(String name) {
        return Optional.ofNullable(parameters.get(name));
    }

    @Override
    public Set<String> getAllParameters() {
        return parameters.keySet();
    }


}
