package org.s4s0l.shathel.commons.scripts

import spock.lang.Specification

/**
 * @author Marcin Wielgus
 */
class GroovyExecutableTest extends Specification {

    def "Script should getGrapes"() {
        given:
        GroovyExecutable ge = new GroovyExecutable()

        when:
        Map<String, String> res = ge.execute(getClass().getResource("/scala.groovy").text, ["input":{ x -> x }])

        then:
        res['groovy'] == 'groovy'
        res['scala'] == 'scala.Option'
    }
}
