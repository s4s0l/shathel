package org.s4s0l.shathel.commons.core

import spock.lang.Specification

/**
 * @author Marcin Wielgus
 */
class ParametersTest extends Specification {

    def "overing should work"(String variable, String expected) {

        given:
        def variables = MapParameters.builder()
                .parameter("sht.env.hides.prop", "from map")
                .parameter("sht.env.hides.map", "from map")
                .parameter("sht.prop.hides.map", "from map")
                .parameter("sht.map.noover", "from map")
                .build().hiddenBySystemProperties().hiddenByVariables();
        System.setProperty("sht.env.hides.prop", "from prop")
        System.setProperty("sht.env.hides.map", "from prop")
        System.setProperty("sht.prop.hides.map", "from prop")
        System.setProperty("sht.prop.noover", "from prop")

        when:
        def parameter = variables.getParameter(variable)

        then:
        parameter.get() == expected

        where:
        variable               | expected
        "sht.env.hides.map"  | "from env"
        "sht.env.hides.prop"  | "from env"
        "sht.prop.hides.map"  | "from prop"
        "sht.env.noover"       | "from env"
        "sht.prop.noover"      | "from prop"
        "sht.map.noover"       | "from map"
    }
}
