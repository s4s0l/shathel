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

    public MapParameters(Map<String, String> parameters) {
        this.parameters = parameters;
    }

    public Optional<String> getParameter(String name) {
        return Optional.ofNullable(Parameters.getNormalizedParameterNames(parameters).get(Parameters.getNormalizedParameterName(name)));
    }

    @Override
    public Set<String> getAllParameters() {
        return Parameters.getNormalizedParameterNames(parameters).keySet();
    }


}
