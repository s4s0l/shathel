package org.s4s0l.shathel.commons.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Marcin Wielgus
 */
public class MultiSourceParameters implements Parameters {
    private final List<Parameters> params;

    public MultiSourceParameters(List<Parameters> params) {
        this.params = params;
    }

    @Override
    public Optional<String> getParameter(String name) {
        return params.stream().map(x -> x.getParameter(name))
                .filter(x -> x.isPresent())
                .findFirst().orElseGet(() -> Optional.empty());
    }

    public Parameters hiddenBy(Parameters x) {
        List<Parameters> nl = new ArrayList<>(params);
        nl.add(0,x);
        return new MultiSourceParameters(nl);
    }

    @Override
    public Set<String> getAllParameters() {
        return params.stream()
                .map(Parameters::getAllParameters)
                .flatMap(x -> x.stream())
                .collect(Collectors.toSet());
    }
}
