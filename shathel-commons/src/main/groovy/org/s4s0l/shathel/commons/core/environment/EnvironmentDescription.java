package org.s4s0l.shathel.commons.core.environment;

import org.s4s0l.shathel.commons.core.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author Marcin Wielgus
 */
public class EnvironmentDescription implements SettingsContainer {
    private final String name;
    private final String type;
    private final Map<String, String> envs;
    private final SolutionDescription solution;
    private final Parameters parameters;

    public EnvironmentDescription(SolutionDescription solution,
                                  String name, String type,
                                  Map<String, String> parameters,
                                  Map<String, String> envs) {
        this.name = name;
        this.type = type;
        this.envs = envs;
        this.solution = solution;
        this.parameters = new MapParameters(
                Parameters.getNormalizedParameterNames(parameters, "shathel.env." + name + ".")
        ).hiddenBy(solution.getParameters());
    }

    public String getEnvironmentName() {
        return name;
    }

    public String getSolutionName() {
        return solution.getSolutionName();
    }

    @Override
    public Parameters getParameters() {
        return this.parameters;
    }

    public Optional<String> getEnvironmentParameter(String name) {
        return getParameters().getParameter("shathel.env." + getEnvironmentName() + "." + name);
    }

    public Optional<Integer> getEnvironmentParameterAsInt(String name) {
        return getEnvironmentParameter(name)
                .map(Integer::parseInt);
    }

    public Optional<Boolean> getEnvironmentParameterAsBoolean(String name) {
        return getEnvironmentParameter(name)
                .map(Boolean::parseBoolean);
    }

    public SolutionDescription getSolution() {
        return solution;
    }


    public String getType() {
        return type;
    }

    public Map<String, String> getAsEnvironmentVariables() {
        Map<String, String> ret = new HashMap<>(solution.getAsEnvironmentVariables());
        ret.putAll(envs);
        String thisEnvPrefix = "shathel.env." + getEnvironmentName() + ".";
        getParameters().getAllParameters().stream()
                .filter(it -> it.startsWith(thisEnvPrefix))
                .map(it -> it.substring(thisEnvPrefix.length()))
                .forEach(it -> ret.put(
                        Parameters.parameterNameToEnvName("shathel.env." + it),
                        getEnvironmentParameter(it).orElse(null)));
        return ret.entrySet().stream().filter(it -> it.getValue() != null)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }


}
