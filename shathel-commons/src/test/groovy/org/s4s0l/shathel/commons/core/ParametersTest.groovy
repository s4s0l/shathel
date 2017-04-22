package org.s4s0l.shathel.commons.core

import spock.lang.Specification
import spock.lang.Unroll

/**
 * @author Marcin Wielgus
 */
class ParametersTest extends Specification {

    @Unroll
    def "overing should work for #variable should return #expected"(String variable, String expected) {

        given:
        def variables = MapParameters.builder()
                .parameter("shathel.env.hides.prop", "from map")
                .parameter("shathel.env.hides.map", "from map")
                .parameter("shathel.prop.hides.map", "from map")
                .parameter("shathel.map.noover", "from map")
                .build().hiddenBySystemProperties().hiddenByVariables();
        System.setProperty("shathel.env.hides.prop", "from prop")
        System.setProperty("shathel.env.hides.map", "from prop")
        System.setProperty("shathel.prop.hides.map", "from prop")
        System.setProperty("shathel.prop.noover", "from prop")

        when:
        def parameter = variables.getParameter(variable)

        then:
        parameter.get() == expected

        where:
        variable                           | expected
        "shathel.env.hides.map"                | "from env"
        "shathel.env.hides.prop"               | "from env"
        "shathel.prop.hides.map"               | "from prop"
        "shathel.env.noover"                   | "from env"
        "shathel.prop.noover"                  | "from prop"
        "shathel.map.noover"                   | "from map"
        "shathel_env.hides.map"                | "from env"
        "shathel.env_hides.prop"               | "from env"
        "shathel.prop.hides_map"               | "from prop"
        "shathel.env_noover"                   | "from env"
        "shathel_prop.noover"                  | "from prop"
        "shathel.map_noover"                   | "from map"
        "shathel.env.hides.map".toUpperCase()  | "from env"
        "shathel.env.hides.prop".toUpperCase() | "from env"
        "shathel.prop.hides.map".toUpperCase() | "from prop"
        "shathel.env.noover".toUpperCase()     | "from env"
        "shathel.prop.noover".toUpperCase()    | "from prop"
        "shathel.map.noover".toUpperCase()     | "from map"
        "shathel_env.hides.map".toUpperCase()  | "from env"
        "shathel.env_hides.prop".toUpperCase() | "from env"
        "shathel.prop.hides_map".toUpperCase() | "from prop"
        "shathel.env_noover".toUpperCase()     | "from env"
        "shathel_prop.noover".toUpperCase()    | "from prop"
        "shathel.map_noover".toUpperCase()     | "from map"
    }
}
