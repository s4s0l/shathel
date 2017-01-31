package org.s4s0l.shathel.commons.utils

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import spock.lang.Specification

/**
 * @author Matcin Wielgus
 */
class ExecWrapperTest extends Specification {
    private static final Logger LOGGER = LoggerFactory.getLogger(ExecWrapperTest.class);
    def "Schould handle exit codes"(){
        given:
        ExecWrapper ew = new ExecWrapper(LOGGER,"ls")

        when:
        def exit = ew.executeForExitValue(new File("src"), "-al")

        then:
        exit == 0

        when:
        exit = ew.executeForExitValue(new File("src"), "-al asdasd")

        then:
        exit != 0
    }

    def "Schould return output"(){
        given:
        ExecWrapper ew = new ExecWrapper(LOGGER,"ls")

        when:
        def exit = ew.executeForOutput(new File("src"), "-al")

        then:
        exit.contains("main")
        exit.contains("test")
    }
}
