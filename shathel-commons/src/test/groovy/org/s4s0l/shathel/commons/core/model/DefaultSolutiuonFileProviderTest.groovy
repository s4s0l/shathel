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
                'shathel.env.local.net'      : '0.0.0',
                'shathel.env.local.safePassword' : 'aaaa',
                'shathel.env.local.shathel_some_secret_path' : 'aaaa',
                'shathel.env.local.token' : 'aaaa',
                'shathel.env.local.key' : 'aaaa',
                'shathel.env.local.shathel_some_secret_value' : 'aaaa'

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
        yaml['shathel-solution']['environments']['local'].net == "0.0.0"
        yaml['shathel-solution']['environments']['local'].safePassword == null
        yaml['shathel-solution']['environments']['local']['shathel.some.secret.path'] == 'aaaa'
        yaml['shathel-solution']['environments']['local']['shathel.some.secret.value'] == null
        yaml['shathel-solution']['environments']['local'].token == null
        yaml['shathel-solution']['environments']['local'].key == null


    }
}
