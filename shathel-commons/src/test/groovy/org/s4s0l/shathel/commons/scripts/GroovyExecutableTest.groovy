package org.s4s0l.shathel.commons.scripts

import org.s4s0l.shathel.commons.scripts.groovy.GroovyExecutable
import spock.lang.Specification

/**
 * @author Marcin Wielgus
 */
class GroovyExecutableTest extends Specification {

    def "Script should getGrapes"() {
        given:
        GroovyExecutable ge = new GroovyExecutable()
        def output = [:]
        when:
        Map<String, String> res = ge.execute(getClass().getResource("/scala.groovy").text, ["input":{ x -> x }, output:output])

        then:
        output['groovy'] == 'groovy'
        output['scala'] == 'scala.Option'
    }
}
