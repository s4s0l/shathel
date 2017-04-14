package org.s4s0l.shathel.commons.remoteswarm

import spock.lang.Specification
import spock.lang.Unroll

/**
 * @author Marcin Wielgus
 */
class ProcessorCommandTest extends Specification {
    @Unroll
    def "toCommand works: on input #command"(String command, Optional<ProcessorCommand> expected) {
        when:
        def res = ProcessorCommand.toCommand(command)

        then:
        res == expected

        where:
        command | expected
        "start" | Optional.of(ProcessorCommand.START)
        "START" | Optional.of(ProcessorCommand.START)
        "xxx"   | Optional.empty()
        null    | Optional.empty()
    }
}
