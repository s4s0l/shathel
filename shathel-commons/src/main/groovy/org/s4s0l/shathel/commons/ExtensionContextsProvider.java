package org.s4s0l.shathel.commons;

import lombok.Builder;
import lombok.Singular;
import org.s4s0l.shathel.commons.core.Parameters;
import org.s4s0l.shathel.commons.utils.ExtensionContext;
import org.s4s0l.shathel.commons.utils.ExtensionInterface;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author Marcin Wielgus
 */
@Builder
public class ExtensionContextsProvider {

    @Singular
    private final List<Function<Parameters, ExtensionInterface>> extensions;

    public ExtensionContext create(Parameters parameters) {

        return new ExtensionContext(extensions.stream().map(it -> it.apply(parameters)).collect(Collectors.toList()));
    }
}
