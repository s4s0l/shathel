package org.s4s0l.shathel.commons.core.environment;

import org.s4s0l.shathel.commons.core.ParameterProvider;
import org.s4s0l.shathel.commons.core.Parameters;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author Marcin Wielgus
 */
public class EnvironmentDescription implements ParameterProvider {
    private final Parameters overrides;
    private final String name;
    private final String type;
    private final Map<String, String> parameters;

    public EnvironmentDescription(Parameters overrides, String name, String type, Map<String, String> parameters) {
        this.overrides = overrides;
        this.name = name;
        this.type = type;
        this.parameters = parameters;
    }

    public String getName() {
        return name;
    }

    public Optional<String> getParameter(String name) {
        String s = overrides.getParameter("shathel.env." + getName() + "." + name).orElseGet(() -> parameters.get(name));
        return Optional.ofNullable(s);
    }

    public int getNodesCount() {
        return getManagersCount() + getWorkersCount();
    }

    public int getManagersCount() {
        return getParameterAsInt("managers")
                .orElse(1);
    }

    public int getWorkersCount() {
        return getParameterAsInt("workers")
                .orElse(0);
    }

    public String getType() {
        return type;
    }


    public Map<String, String> getAsEnvironmentVariables() {
        Map<String, String> ret = new HashMap<>();

        parameters.entrySet()
                .forEach(x ->
                        ret.put(
                                Parameters.parameterNameToEnvName("shathel.env." + x.getKey()),
                                getParameter(x.getKey()).orElse(null)));
        String thisEnvPrefix = "shathel.env." + getName() + ".";
        overrides.getAllParameters().stream()
                .filter(it -> it.startsWith(thisEnvPrefix))
                .map(it -> it.substring(thisEnvPrefix.length()))
//                .filter(it -> !ret.containsKey(Parameters.parameterNameToEnvName("shathel.env." + it)))
                .forEach(it -> ret.put(
                        Parameters.parameterNameToEnvName("shathel.env." + it),
                        getParameter(it).orElse(null)));


        addCalculatedEnvs(ret);

        return ret.entrySet().stream().filter(it -> it.getValue() != null)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));


    }

    public void addCalculatedEnvs(Map<String, String> ret) {
        int size = getNodesCount();
        int quorum = (int) Math.floor(size / 2) + 1;

        ret.put("SHATHEL_ENV_SIZE", "" + size);
        ret.put("SHATHEL_ENV_QUORUM", "" + quorum);

        int msize = getManagersCount();
        int mquorum = (int) Math.floor(msize / 2) + 1;

        ret.put("SHATHEL_ENV_MGM_SIZE", "" + msize);
        ret.put("SHATHEL_ENV_MGM_QUORUM", "" + mquorum);
        ret.put("SHATHEL_ENV_DOMAIN", getParameter("domain").orElse("localhost"));
    }

}
