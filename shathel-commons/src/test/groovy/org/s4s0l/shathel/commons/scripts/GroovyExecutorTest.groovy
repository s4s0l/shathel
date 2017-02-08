package org.s4s0l.shathel.commons.scripts

import spock.lang.Specification

/**
 * @author Matcin Wielgus
 */
class GroovyExecutorTest extends Specification {

    def "Script should getGrapes"() {
        given:
        GroovyExecutor ge = new GroovyExecutor()

        when:
        Map<String, String> res = ge.execute(getClass().getResource("/scala.groovy").text, ["input":{ x -> x }])

        then:
        res['groovy'] == 'groovy'
        res['scala'] == 'scala.Option'
    }
}
