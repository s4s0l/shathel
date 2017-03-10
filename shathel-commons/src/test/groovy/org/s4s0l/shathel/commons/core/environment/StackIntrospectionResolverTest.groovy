package org.s4s0l.shathel.commons.core.environment

import spock.lang.Specification

/**
 * @author Matcin Wielgus
 */
class StackIntrospectionResolverTest extends Specification {

    def "Should collect consistent values"() {
        given:
        def labels = ["org.shathel.stack.a"  : 1,
                      "org.shathel.stack.ga" : "GA",
                      "org.shathel.stack.gav": "GAV",
                      "org.shathel.stack.b"  : 1]
        StackIntrospectionResolver resolver = new StackIntrospectionResolver([
                labels, [:] <<labels
        ])

        when:
        def ga = resolver.getGa()
        def gav = resolver.getGav()
        def rlabels = resolver.getShathelLabels()

        then:
        ga == "GA"
        gav == "GAV"
        rlabels == labels
    }

    def "Should collect distinct label names"() {
        given:
        def labels = ["org.shathel.stack.a"  : 1,
                      "org.shathel.stack.ga" : "GA",
                      "org.shathel.stack.gav": "GAV",
                        "com.docker.compose.service" : "service1",
                      "org.shathel.stack.b"  : 1]
        StackIntrospectionResolver resolver = new StackIntrospectionResolver([
                labels, [:] <<labels, ([:] << labels) << ["com.docker.compose.service" : "service2"]
        ])

        when:
        def ga = resolver.getLabelValues("com.docker.compose.service")

        then:
        ga == ["service1":2, "service2":1]
    }


}
