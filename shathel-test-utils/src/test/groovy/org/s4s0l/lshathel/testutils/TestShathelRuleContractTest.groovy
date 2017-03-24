package org.s4s0l.lshathel.testutils

import org.junit.runners.model.Statement
import org.mockito.ArgumentCaptor
import org.mockito.Mockito
import org.s4s0l.shathel.commons.core.CommonParams
import org.s4s0l.shathel.commons.core.dependencies.LocalOverriderDownloader
import org.s4s0l.shathel.commons.docker.DockerWrapper
import spock.lang.Specification

/**
 * @author Marcin Wielgus
 */
class TestShathelRuleContractTest extends Specification {

    def cleanupSpec() {
        setupSpec();
    }

    def setupSpec() {
        new DockerWrapper().with {
            if (swarmActive()) {
                stackUnDeploy(new File("."), "IntrospectionStack")
            }
        }
    }


    def "Quick test run"() {
        given:
        System.setProperty("shathel.plugin.ip", "localhost")
        System.setProperty(CommonParams.SHATHEL_ENV, "local")
        System.setProperty("shathel.plugin.local.override.mappings", "src/test/resources/TestShathelRuleContractTest/mappings")
        System.setProperty("shathel.plugin.local.override.current", "src/test/resources/TestShathelRuleContractTest/stack")
        System.setProperty("shathel.plugin.current.gav", "org.s4s0l.shathel:introspection:1.0")
        System.setProperty("shathel.plugin.current", LocalOverriderDownloader.CURRENT_PROJECT_LOCATION)
        System.setProperty("shathel.env.local.dependenciesDir", "build/.shathel-dependency-cache")
        System.setProperty(CommonParams.SHATHEL_DIR, "build/.shathel")
        ArgumentCaptor<VerifierContextContract> argument = ArgumentCaptor.forClass(VerifierContextContract.class);

        def verifier = Mockito.mock(Verifier)
        Mockito.when(verifier.verify(Mockito.any())).thenReturn(true)

        def build = TestShathelRule.builder()
                .addVerifier(verifier)
                .build()
        def statement = Mockito.mock(Statement)
        when:
        build.apply(statement, null).evaluate()
        Mockito.verify(verifier).verify(argument.capture())

        then:
        argument.getValue().ip() == "localhost"
        argument.getValue().fill("http://\${IP}:\${DUMMYSERVICE_4000}") == "http://localhost:9999"
        argument.getValue().fill("http://\${IP}:\${INTROSPECTIONSTACK_DUMMYSERVICE_4000}") == "http://localhost:9999"
    }
}
