package org.s4s0l.shathel.commons.core.stack;

import org.s4s0l.shathel.commons.core.ParameterProvider;
import org.s4s0l.shathel.commons.core.Parameters;

import java.util.Map;
import java.util.Optional;

/**
 * @author Marcin Wielgus
 */
public class SolutionStackDesctiption implements ParameterProvider {
    private final StackReference reference;
    private final Map<String, String> environments;
    private final Map<String, String> params;
    private final Parameters overrides;

    public SolutionStackDesctiption(StackReference reference, Map<String, String> environments, Map<String, String> params, Parameters overrides) {
        this.reference = reference;
        this.environments = environments;
        this.params = Parameters.getNormalizedParameterNames(params);
        this.overrides = overrides;
    }

    @Override
    public Optional<String> getParameter(String name) {
        String normalizedParameterName = Parameters.getNormalizedParameterName(name);
        String s = overrides.getParameter("shathel." + reference.getName() + "." + normalizedParameterName).orElseGet(() -> params.get(normalizedParameterName));
        return Optional.ofNullable(s);
    }

    public Map<String, String> getEnvironments() {
        return environments;
    }
}
