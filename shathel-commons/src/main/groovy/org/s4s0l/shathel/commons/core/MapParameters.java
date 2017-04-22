package org.s4s0l.shathel.commons.core;

import lombok.Builder;
import lombok.Singular;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * @author Marcin Wielgus
 */

@Builder
public class MapParameters implements Parameters {
    //TODO: lombok sucks change it to immutables
    @Singular
    private final Map<String, String> parameters;


    public Optional<String> getParameter(String name) {
        return Optional.ofNullable(Parameters.getNormalizedParameterNames(parameters).get(Parameters.getNormalizedParameterName(name)));
    }

    @Override
    public Set<String> getAllParameters() {
        return Parameters.getNormalizedParameterNames(parameters).keySet();
    }


}
