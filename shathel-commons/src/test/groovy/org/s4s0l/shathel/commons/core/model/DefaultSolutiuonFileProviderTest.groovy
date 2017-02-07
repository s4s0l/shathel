package org.s4s0l.shathel.commons.core.model

import org.s4s0l.shathel.commons.core.Parameters
import org.yaml.snakeyaml.Yaml
import spock.lang.Specification

/**
 * @author Matcin Wielgus
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
                'shathel.env.dev.net'      : '0.0.0',

        ])

        when:
        def config = dsfp.getDefaultConfig("PName", params)
        def yaml = new Yaml().load(config)

        then:
        yaml['shathel-solution']['name'] == "PName"
        yaml['shathel-solution']['environments']['composed'].type == "docker-compose"
        yaml['shathel-solution']['environments']['extraenv'].type == "dind"
        yaml['shathel-solution']['environments']['extraenv'].net == "0.0.0"
        yaml['shathel-solution']['environments']['xxxx'] == null
        yaml['shathel-solution']['environments']['dev'].net == "0.0.0"


    }
}
