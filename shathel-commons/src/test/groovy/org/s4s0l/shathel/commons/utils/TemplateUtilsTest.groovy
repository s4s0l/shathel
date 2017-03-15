package org.s4s0l.shathel.commons.utils

import spock.lang.Specification
import spock.lang.Unroll

/**
 * @author Marcin Wielgus
 */
class TemplateUtilsTest extends Specification {
    @Unroll
    def "FillEnvironmentVariables when #text then should give #expected"(text, expected) {
        given:
        def envs = [VAR:'value']
        when:
        def filled = TemplateUtils.fillEnvironmentVariables(text, envs)

        then:
        filled == expected

        where:

        text | expected
        "this is \${VAR}a" | "this is valuea"
        "this is \${VAR} a" | "this is value a"
        "this is \${VAR} and \${VAR}" | "this is value and value"
        "this is \${VAR} and \${VARX:-default}x" | "this is value and defaultx"
        "this is \$VAR" | "this is value"
        "this is novar" | "this is novar"
        "this is \$\$VAR" | "this is \$\$VAR"
        "this is \${VAR:-default}" | "this is value"
        "this is \${VAR2:-default}" | "this is default"

    }
}
