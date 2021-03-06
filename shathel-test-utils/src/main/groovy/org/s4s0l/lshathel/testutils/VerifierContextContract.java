package org.s4s0l.lshathel.testutils;

import org.immutables.value.Value;
import org.s4s0l.shathel.commons.core.environment.Environment;
import org.s4s0l.shathel.commons.core.environment.ExecutableApiFacade;
import org.s4s0l.shathel.commons.core.environment.StackIntrospection;
import org.s4s0l.shathel.commons.core.environment.StackIntrospectionProvider;
import org.s4s0l.shathel.commons.utils.TemplateUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Marcin Wielgus
 */
@Value.Immutable
@Value.Style(depluralize = true, typeAbstract = "*Contract", typeImmutable = "*")
public interface VerifierContextContract {

    StackIntrospectionProvider introspectionProvider();

    ExecutableApiFacade api();

    @Value.Derived
    default Map<String, String> variables() {
        List<StackIntrospection> allStacks = introspectionProvider().getAllStacks().getStacks();
        Map<String, String> ret = new HashMap<>();

        for (StackIntrospection allStack : allStacks) {
            for (StackIntrospection.Service service : allStack.getServices()) {
                String servicePrefix = service.getServiceName().replaceAll("[^0-9a-zA-Z]", "_").toUpperCase() + "_";
                String fullNamePrefix = service.getFullServiceName().replaceAll("[^0-9a-zA-Z]", "_").toUpperCase() + "_";
                for (Map.Entry<Integer, Integer> entry : service.getPortMapping().entrySet()) {
                    String tunneledPort = api().openPublishedPort(entry.getValue());
                    ret.put(servicePrefix + entry.getKey(), tunneledPort);
                    ret.put(fullNamePrefix + entry.getKey(), tunneledPort);
                }
            }
        }
        return ret;
    }

    default String fill(String pattern) {
        return TemplateUtils.fillEnvironmentVariables(pattern, variables());
    }


}
