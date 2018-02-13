package org.s4s0l.shathel.commons.core.environment

import org.s4s0l.shathel.commons.core.Parameters
import org.s4s0l.shathel.commons.core.SolutionDescription
import org.s4s0l.shathel.commons.core.model.SolutionFileModel
import spock.lang.Specification

/**
 * @author Marcin Wielgus
 */
class EnvironmentDescriptionTest extends Specification {

    def "environment map should contain all not null parameters and globals matching env name prefix"() {
        given:
        def env = Parameters.fromMapWithSysPropAndEnv(['shathel.env.test.sysprop': 'sysprop'])
        EnvironmentDescription des = new EnvironmentDescription(
                new SolutionDescription(env, new SolutionFileModel([version: 1, 'shathel-solution': [:]])), "test", "type", ['file_prop': 'file', 'sysprop': 'file'], ['AAA': 'AAA_VALUE'])


        when:
        def variabbles = des.getAsEnvironmentVariables()

        then:
        variabbles == [
                'SHATHEL_ENV_FILE_PROP' : 'file',
                'SHATHEL_ENV_SYSPROP'   : 'sysprop',
                'SHATHEL_ENV_ENVPROP'   : 'envprop',
//                'SHATHEL_ENV_SIZE'      : '1',
//                'SHATHEL_ENV_QUORUM'    : '1',
//                'SHATHEL_ENV_MGM_SIZE'  : '1',
//                'SHATHEL_ENV_MGM_QUORUM': '1',
//                'SHATHEL_ENV_DOMAIN'    : 'localhost',
                'AAA'                   : 'AAA_VALUE',
        ]

        when:
        def val = des.getEnvironmentParameter("File.prop")

        then:
        val.get() == "file"
    }
}
