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
        def parameters = ['file_prop': 'file', 'sysprop': 'file']
        def envs = ['AAA': 'AAA_VALUE']
        EnvironmentDescription des = new EnvironmentDescription(
                new SolutionDescription(env, new SolutionFileModel([version: 1, 'shathel-solution': [:]])), "test", "type", parameters, envs)


        when:
        def variabbles = des.getAsEnvironmentVariables()

        then:
        variabbles['SHATHEL_ENV_FILE_PROP'] == 'file'
        variabbles['SHATHEL_ENV_SYSPROP'] == 'sysprop'
        variabbles['SHATHEL_ENV_ENVPROP'] == 'envprop'
        variabbles['AAA'] == 'AAA_VALUE'

        when:
        def val = des.getEnvironmentParameter("File.prop")

        then:
        val.get() == "file"
    }
}
