package org.s4s0l.lshathel.testutils;

import org.immutables.value.Value;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.s4s0l.shathel.commons.core.dependencies.LocalOverriderDownloader;
import org.s4s0l.shathel.commons.ssh.SshTunelManagerImpl;
import org.s4s0l.shathel.commons.utils.ExtensionInterface;
import org.s4s0l.shathel.commons.utils.TemplateUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Marcin Wielgus
 */
@Value.Immutable
@Value.Style(
        depluralize = true,
//        typeBuilder = "BuilderFor_*",
        typeAbstract = "*Contract",
        strictBuilder = true,
        typeImmutable = "*"
)
public interface TestShathelRuleContract extends SchathelCreationCommonContract, TestRule {

    List<String> stacks();

    List<Verifier> verifiers();

    static Verifier waitForHttp(String addressTemplate, List<Integer> httpCodesAccepted) {
        return new WaitForHttpServiceVerifier(addressTemplate, httpCodesAccepted);
    }

    @Value.Derived
    default TestShathelContract testShathel() {
        List<ExtensionInterface> extensions = new ArrayList<>();
        extensions.add(new LocalOverriderDownloader(mappingsDirectory(), currentStackDirectory()));
        extensions.addAll(extensions());
        return TestShathel.builder()
                .addAllExtensions(extensions)
                .putAllParams(params())
                .build();
    }


    @Value.Derived
    default File mappingsDirectory() {
        return new File(parameters().getParameter("shathel.plugin.local.override.mappings").orElse(".shathel-mappings"));
    }

    @Value.Derived
    default File currentStackDirectory() {
        return new File(parameters().getParameter("shathel.plugin.local.override.current").orElse("."));
    }


    @Value.Derived
    default String currentStackGav() {
        return parameters().getParameter("shathel.plugin.current.gav").get();
    }

    @Value.Derived
    default String currentStack() {
        return parameters().getParameter("shathel.plugin.current").orElse(LocalOverriderDownloader.CURRENT_PROJECT_LOCATION);
    }

    @Value.Default
    default boolean withDependencies() {
        return true;
    }

    @Value.Default
    default boolean withOptionalDependencies() {
        return false;
    }

    @Value.Default
    default long verifierInterval() {
        return 1000;
    }

    @Value.Default
    default long verifierAttempts() {
        return 45;
    }

    @Value.Lazy
    default VerifierContextContract verifierContext() {
        return VerifierContext.builder()
                .api(testShathel().environment().getEnvironmentApiFacade())
                .introspectionProvider(testShathel().environment().getIntrospectionProvider())
                .build();
    }

    default String fill(String pattern) {
        return verifierContext().fill(pattern);
    }

    default void verify() {
        VerifierContextContract verifierContext = verifierContext();
        verifiers:
        for (Verifier verifier : verifiers()) {
            Exception lastException = null;
            for (int i = 0; i < verifierAttempts(); i++) {
                lastException = null;
                try {
                    boolean verified = verifier.verify(verifierContext);
                    if (verified) {
                        continue verifiers;
                    }
                } catch (Exception ex) {
                    lastException = ex;
                }
                try {
                    Thread.sleep(verifierInterval());
                } catch (InterruptedException e1) {
                    throw new RuntimeException(e1);
                }
            }
            if (lastException != null) {
                throw new RuntimeException("Verifier failed " + verifier.getClass().getSimpleName(), lastException);
            }
        }
    }

    default void start() {
        if (stacks().isEmpty()) {
            testShathel().start(currentStack(), withOptionalDependencies());
        } else {
            stacks().stream().forEach(it -> testShathel().start(it, withOptionalDependencies()));
        }
    }

    default void stop() {
        if (stacks().isEmpty()) {
            testShathel().stop(currentStack(), withDependencies(), withOptionalDependencies());
        } else {
            stacks().stream().forEach(it -> testShathel().stop(it, withDependencies(), withOptionalDependencies()));
        }
    }

    default Statement apply(Statement base, Description description) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                try {
                    start();
                    verify();
                    base.evaluate();
                } finally {
                    try {
                        stop();
                    } finally {
                        SshTunelManagerImpl.globalCloseAll();
                    }
                }
            }
        };
    }

}
