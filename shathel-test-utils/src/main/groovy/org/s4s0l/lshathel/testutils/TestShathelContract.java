package org.s4s0l.lshathel.testutils;

import org.immutables.value.Value;
import org.s4s0l.shathel.commons.DefaultExtensionContext;
import org.s4s0l.shathel.commons.ExtensionContextsProvider;
import org.s4s0l.shathel.commons.Shathel;
import org.s4s0l.shathel.commons.core.CommonParams;
import org.s4s0l.shathel.commons.core.Solution;
import org.s4s0l.shathel.commons.core.Stacks;
import org.s4s0l.shathel.commons.core.dependencies.StackLocator;
import org.s4s0l.shathel.commons.core.environment.Environment;
import org.s4s0l.shathel.commons.core.environment.EnvironmentContextInternal;
import org.s4s0l.shathel.commons.core.storage.Storage;

import java.io.File;

/**
 * @author Marcin Wielgus
 */
@Value.Immutable
@Value.Style(depluralize = true, typeAbstract = "*Contract", typeImmutable = "*")
public interface TestShathelContract extends SchathelCreationCommonContract {

    @Value.Derived
    default File shathelDir() {
        return new File(parameters().getParameter(CommonParams.SHATHEL_DIR).orElse(".shathel"));
    }

    @Value.Lazy
    default ExtensionContextsProvider extensionContext() {
        return DefaultExtensionContext.create(extensions());
    }

    @Value.Derived
    default Shathel shathel() {
        return new Shathel(parameters(), extensionContext());
    }

    @Value.Derived
    default Storage storage() {
        return shathel().initStorage(shathelDir(), false);
    }

    @Value.Derived
    default Solution solution() {
        return shathel().getSolution(storage());
    }

    @Value.Derived
    default Environment environment() {
        Environment e = solution().getEnvironment(shathelEnv());
        boolean initEnabled = ((EnvironmentContextInternal)e.getEnvironmentContext())
                .getEnvironmentParameterAsBoolean("init")
                .orElse(true);
        if (initEnabled && !e.isInitialized()) {
            e.initialize();
        }
        return e;
    }

    default Stacks stack(String gav) {
        return solution().openStack( new StackLocator(gav));
    }


    default void start(String gav, boolean withOptional) {
        Stacks stack = stack(gav);
        solution().run(stack.createStartCommand(withOptional, environment()));
    }


    default void stop(String gav, boolean withDependencies, boolean withOptional) {
        Stacks stack = stack(gav);
        solution().run(stack.createStopCommand(withDependencies, withOptional,environment()));
    }

}
