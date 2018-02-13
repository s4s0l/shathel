package org.s4s0l.shathel.commons.core;

import org.s4s0l.shathel.commons.core.environment.EnvironmentDescription;
import org.s4s0l.shathel.commons.core.model.SolutionFileModel;

import org.s4s0l.shathel.commons.core.stack.StackReference;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Marcin Wielgus
 */
public class SolutionDescription implements SettingsContainer {
    private final Parameters parameters;
    private final SolutionFileModel model;

    public SolutionDescription(Parameters overrides, SolutionFileModel model) {
        this.model = model;
        this.parameters = new MapParameters(
                Parameters.getNormalizedParameterNames(model.getSolutionParameters(), "shathel.solution.")
        ).hiddenBy(overrides);
    }

    public Optional<DockerLoginInfo> getDockerLoginInfo() {
        Optional<String> dockerUser = getSolutionParameter("docker_user");
        Optional<String> dockerPass = getSolutionParameter("docker_pass");
        Optional<String> dockerHost = getSolutionParameter("docker_host");
        if (dockerUser.isPresent() && dockerPass.isPresent()) {
            return Optional.of(new DockerLoginInfo(dockerUser.get(), dockerPass.get(), dockerHost));
        } else {
            return Optional.empty();
        }
    }

    @Override
    public Parameters getParameters() {
        return parameters;
    }

    public Optional<String> getSolutionParameter(String name) {
        return getParameters().getParameter("shathel.solution." + name);
    }

    public Optional<Integer> getSolutionParameterAsInt(String name) {
        return getSolutionParameter(name)
                .map(Integer::parseInt);
    }

    public Optional<Boolean> getSolutionParameterAsBoolean(String name) {
        return getSolutionParameter(name)
                .map(Boolean::parseBoolean);
    }

    public EnvironmentDescription getEnvironmentDescription(String envName) {
        Map<String, String> x = model.getEnvironment(envName);
        Map<String, String> envEnvs = model.getEnvironmentEnvs(envName);
        return new EnvironmentDescription(this, x.get("name"), x.get("type"), x, envEnvs);
    }

    public String getSolutionName() {
        return model.getName();
    }

    public Set<String> getEnvironments() {
        return model.getEnvironments();
    }

    public Map<String, String> getSolutionStackEnvs(StackReference stackReference) {
        Map<String, Map<String, String>> stack = model.getStack(stackReference.getName());
        return stack.get("envs");
    }

    @Override
    public Map<String, String> getAsEnvironmentVariables() {
        Map<String, String> ret = new HashMap<>(model.getEnvs());
        ret.put(Parameters.parameterNameToEnvName("shathel.solution.name"), getSolutionName());
        String thisEnvPrefix = "shathel.solution.";
        parameters.getAllParameters().stream()
                .filter(it -> it.startsWith(thisEnvPrefix))
                .map(it -> it.substring(thisEnvPrefix.length()))
                .forEach(it -> ret.put(
                        Parameters.parameterNameToEnvName("shathel.solution." + it),
                        getSolutionParameter(it).orElse(null)));

        return ret.entrySet().stream().filter(it -> it.getValue() != null)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

}
