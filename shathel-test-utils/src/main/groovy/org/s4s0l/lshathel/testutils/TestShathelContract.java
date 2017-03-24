package org.s4s0l.lshathel.testutils;

import org.immutables.value.Value;
import org.s4s0l.shathel.commons.DefaultExtensionContext;
import org.s4s0l.shathel.commons.Shathel;
import org.s4s0l.shathel.commons.core.CommonParams;
import org.s4s0l.shathel.commons.core.Parameters;
import org.s4s0l.shathel.commons.core.Solution;
import org.s4s0l.shathel.commons.core.Stack;
import org.s4s0l.shathel.commons.core.dependencies.LocalOverriderDownloader;
import org.s4s0l.shathel.commons.core.dependencies.StackLocator;
import org.s4s0l.shathel.commons.core.environment.Environment;
import org.s4s0l.shathel.commons.core.storage.Storage;
import org.s4s0l.shathel.commons.utils.ExtensionContext;
import org.s4s0l.shathel.commons.utils.ExtensionInterface;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

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

    @Value.Derived
    default boolean isShathelInitEnabled() {
        return parameters().getParameterAsBoolean("shathel.env."+shathelEnv()+".init")
                .orElse(true);
    }

    @Value.Lazy
    default ExtensionContext extensionContext() {
        return DefaultExtensionContext.create(parameters(), extensions());
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
        if (isShathelInitEnabled() && !e.isInitialized()) {
            e.initialize();
        }
        return e;
    }

    default Stack stack(String gav) {
        return solution().openStack(environment(), new StackLocator(gav));
    }


    default void start(String gav, boolean withOptional) {
        Stack stack = stack(gav);
        stack.run(stack.createStartCommand(withOptional));
    }


    default void stop(String gav, boolean withDependencies, boolean withOptional) {
        Stack stack = stack(gav);
        stack.run(stack.createStopCommand(withDependencies, withOptional));
    }

}
