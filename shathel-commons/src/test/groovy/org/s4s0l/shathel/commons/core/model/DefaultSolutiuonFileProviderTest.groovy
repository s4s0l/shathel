package org.s4s0l.shathel.commons.core.model

import org.s4s0l.shathel.commons.core.Parameters
import org.yaml.snakeyaml.Yaml
import spock.lang.Specification

/**
 * @author Marcin Wielgus
 */
class DefaultSolutiuonFileProviderTest extends Specification {
    def "GetDefaultConfig"() {
        given:
        def dsfp = new DefaultSolutiuonFileProvider()
        def params = Parameters.fromMapWithSysPropAndEnv([
                'shathel.solution.name'    : 'DummyName',
                'shathel.env.extraenv.type': 'dind',
                'shathel.env.extraenv.net' : '0.0.0',
                'shathel.env.xxxx.net'     : '10.10.10',
                'shathel.env.vbox.net'      : '0.0.0',
                'shathel.env.vbox.safePassword' : 'aaaa',
                'shathel.env.vbox.shathel_some_secret_path' : 'aaaa',
                'shathel.env.vbox.shathel_some_secret_value' : 'aaaa'

        ])

        when:
        def config = dsfp.getDefaultConfig("PName", params)
        def yaml = new Yaml().load(config)

        then:
        yaml['shathel-solution']['name'] == "PName"
        yaml['shathel-solution']['environments']['local'].type == "local-swarm"
        yaml['shathel-solution']['environments']['extraenv'].type == "dind"
        yaml['shathel-solution']['environments']['extraenv'].net == "0.0.0"
        yaml['shathel-solution']['environments']['xxxx'] == null
        yaml['shathel-solution']['environments']['vbox'].net == "0.0.0"
        yaml['shathel-solution']['environments']['vbox'].safePassword == null
        yaml['shathel-solution']['environments']['vbox'].shathel_some_secret_path == 'aaaa'
        yaml['shathel-solution']['environments']['vbox'].shathel_some_secret_value == null


    }
}
