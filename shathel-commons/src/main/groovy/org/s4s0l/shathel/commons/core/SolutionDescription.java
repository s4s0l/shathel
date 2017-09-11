package org.s4s0l.shathel.commons.core;

import org.s4s0l.shathel.commons.core.environment.EnvironmentDescription;
import org.s4s0l.shathel.commons.core.model.SolutionFileModel;
import org.s4s0l.shathel.commons.core.stack.SolutionStackDesctiption;
import org.s4s0l.shathel.commons.core.stack.StackReference;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Marcin Wielgus
 */
public class SolutionDescription {
    private final Parameters overrides;
    private final SolutionFileModel model;
    private final Map<String, String> parameters;

    public SolutionDescription(Parameters overrides, SolutionFileModel model) {
        this.overrides = overrides;
        this.model = model;
        this.parameters = Parameters.getNormalizedParameterNames(model.getSolutionParameters());
    }

    public EnvironmentDescription getEnvironmentDescription(String envName) {
        Map<String, String> x = model.getEnvironment(envName);
        Map<String,String> envEnvs = model.getEnvironmentEnvs(envName);
        return new EnvironmentDescription(overrides, x.get("name"), x.get("type"), x, envEnvs);
    }

    public String getName() {
        return model.getName();
    }

    public Optional<String> getParameter(String name) {
        String normalizedParameterName = Parameters.getNormalizedParameterName(name);
        String s = overrides.getParameter("shathel.solution." + normalizedParameterName).orElseGet(() -> parameters.get(normalizedParameterName));
        return Optional.ofNullable(s);
    }

    public Set<String> getEnvironments() {
        return model.getEnvironments();
    }

    public SolutionStackDesctiption getSolutionStackDescription(StackReference stackReference) {
        Map<String, Map<String, String>> stack = model.getStack(stackReference.getName());
        return new SolutionStackDesctiption(
                stackReference,
                stack.get("envs"),
                stack.get("params"),
                overrides);

    }

    public Map<String, String> getAsEnvironmentVariables() {
        Map<String, String> ret = new HashMap<>(getEnvs());
        ret.put(Parameters.parameterNameToEnvName("shathel.solution.name"), getName());
        parameters.forEach((key, value) -> ret.put(
                Parameters.parameterNameToEnvName("shathel.solution." + key),
                getParameter(key).orElse(null)));
        String thisEnvPrefix = "shathel.solution.";
        overrides.getAllParameters().stream()
                .filter(it -> it.startsWith(thisEnvPrefix))
                .map(it -> it.substring(thisEnvPrefix.length()))
//                .filter(it -> !ret.containsKey(Parameters.parameterNameToEnvName("shathel.solution." + it)))
                .forEach(it -> ret.put(
                        Parameters.parameterNameToEnvName("shathel.solution." + it),
                        getParameter(it).orElse(null)));

        return ret.entrySet().stream().filter(it -> it.getValue() != null)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    public Map<String, String> getEnvs() {
        return model.getEnvs();
    }
}
