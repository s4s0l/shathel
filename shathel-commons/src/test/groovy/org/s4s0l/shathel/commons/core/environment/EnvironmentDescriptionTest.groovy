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
        EnvironmentDescription des = new EnvironmentDescription(env, "test", "type", ['file': 'file', 'sysprop': 'file'])


        when:
        def variabbles = des.getAsEnvironmentVariables()

        then:
        variabbles == [
                'SHATHEL_ENV_FILE'   : 'file',
                'SHATHEL_ENV_SYSPROP': 'sysprop',
                'SHATHEL_ENV_ENVPROP': 'envprop',
        ]
    }
}
