package org.s4s0l.shathel.commons.core

import spock.lang.Specification

/**
 * @author Marcin Wielgus
 */
class ParametersTest extends Specification {

    def "overing should work"(String variable, String expected) {

        given:
        def variables = MapParameters.builder()
                .parameter("sht.env.over.by.map", "from map")
                .parameter("sht.prop.over.by.map", "from map")
                .parameter("sht.map.noover", "from map")
                .build().overSystemProperties().overEnvVariables();
        System.setProperty("sht.env.over.by.prop", "from prop")
        System.setProperty("sht.prop.over.by.map", "from prop")
        System.setProperty("sht.prop.noover", "from prop")
        System.setProperty("sht.map.noover", "from prop")

        when:
        def parameter = variables.getParameter(variable)

        then:
        parameter.get() == expected

        where:
        variable               | expected
        "sht.env.over.by.map"  | "from map"
        "sht.env.over.by.prop" | "from prop"
        "sht.prop.over.by.map" | "from map"
        "sht.env.noover"       | "from env"
        "sht.prop.noover"      | "from prop"
        "sht.map.noover"       | "from map"
    }
}
