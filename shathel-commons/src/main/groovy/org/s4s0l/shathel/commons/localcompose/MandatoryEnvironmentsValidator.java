package org.s4s0l.shathel.commons.localcompose;

import org.s4s0l.shathel.commons.core.environment.EnricherExecutable;
import org.s4s0l.shathel.commons.core.environment.EnricherExecutableParams;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * @author Marcin Wielgus
 */
public class MandatoryEnvironmentsValidator extends EnricherExecutable {
    private static final Logger LOGGER = getLogger(MandatoryEnvironmentsValidator.class);

    public MandatoryEnvironmentsValidator() {

    }

    @Override
    protected void execute(EnricherExecutableParams paramz) {
        Map<String, String> mandatory = paramz.getStack().getMandatoryEnvs();
        Set<String> strings = mandatory.keySet();
        List<String> missingVariables = new ArrayList<>();
        for (String string : strings) {
            if (!paramz.getEnvironment().containsKey(string)) {
                LOGGER.error("Mandatory environment variable {} not found, desc: {}", string, mandatory.get(string));
                missingVariables.add(string);
            }
        }
        if (!missingVariables.isEmpty()) {
            throw new RuntimeException("Missing env vars: " + missingVariables.stream().collect(Collectors.joining(", ")));
        }

    }
}