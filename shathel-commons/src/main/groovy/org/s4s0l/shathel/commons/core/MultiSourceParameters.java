package org.s4s0l.shathel.commons.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * @author Matcin Wielgus
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

    public Parameters over(Parameters x) {
        List<Parameters> nl = new ArrayList<>(params);
        nl.add(x);
        return new MultiSourceParameters(nl);
    }
}
