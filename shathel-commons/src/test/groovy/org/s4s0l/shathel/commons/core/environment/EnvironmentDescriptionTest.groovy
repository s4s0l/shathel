package org.s4s0l.shathel.commons.core.environment

import org.s4s0l.shathel.commons.core.Parameters
import spock.lang.Specification

/**
 * @author Marcin Wielgus
 */
class EnvironmentDescriptionTest extends Specification {

    def "environment map should contain all not null parameters and globals matching env name prefix"() {
        given:
        def env = Parameters.fromMapWithSysPropAndEnv(['shathel.env.test.sysprop': 'sysprop'])
        EnvironmentDescription des = new EnvironmentDescription(env, "test", "type", ['file_prop': 'file', 'sysprop': 'file'])


        when:
        def variabbles = des.getAsEnvironmentVariables()

        then:
        variabbles == [
                'SHATHEL_ENV_FILE_PROP'      : 'file',
                'SHATHEL_ENV_SYSPROP'   : 'sysprop',
                'SHATHEL_ENV_ENVPROP'   : 'envprop',
                'SHATHEL_ENV_SIZE'      : '1',
                'SHATHEL_ENV_QUORUM'    : '1',
                'SHATHEL_ENV_MGM_SIZE'  : '1',
                'SHATHEL_ENV_MGM_QUORUM': '1',
                'SHATHEL_ENV_DOMAIN'    : 'localhost',
        ]

        when:
        def val = des.getParameter("File.prop")

        then:
        val.get() == "file"
    }
}
