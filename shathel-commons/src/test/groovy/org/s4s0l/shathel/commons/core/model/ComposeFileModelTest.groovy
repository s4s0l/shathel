package org.s4s0l.shathel.commons.core.model

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.yaml.snakeyaml.Yaml
import spock.lang.Specification

/**
 * @author Matcin Wielgus
 */
class ComposeFileModelTest extends Specification {
    def "ReplaceInAllStrings"() {
        given:
        ComposeFileModel x = new ComposeFileModel(new Yaml().load("""
        a:
            b:
                c: aaXXXbb
                d: XX
            c:
                - vXX
                - a:
                    b: XXb
                    v: lalala   
                  c: xx
                  d: XXd  
        """))

        when:
        x.replaceInAllStrings("XX", "YY")
        def dump = new Yaml().dump(x.parsedYml)
        println dump
        def res = new Yaml().load(dump)


        then:
        res.a.b.c == "aaYYXbb"
        res.a.b.d == "YY"
        res.a.c[0] == "vYY"
        res.a.c[1].a.b == 'YYb'
        res.a.c[1].a.v == 'lalala'
        res.a.c[1].c == 'xx'
        res.a.c[1].d == 'YYd'

    }

    def "Map mounts"() {
        given:
        ComposeFileModel x = new ComposeFileModel(new Yaml().load("""
        services:
              logstash:
                volumes:
                  - ./logstash/config:/etc/logstash/conf.d
              kibana:
                build: kibana/
                volumes:
                  - ./kibana/config/:/etc/kibana/
                ports:
                  - "5601:5601"
        """))

        when:
        x.mapMounts {
            s, v ->
                "$s:$v"
        }

        then:
        x.parsedYml.services.logstash.volumes == ['logstash:./logstash/config:/etc/logstash/conf.d']
        x.parsedYml.services.kibana.volumes == ['kibana:./kibana/config/:/etc/kibana/']
    }
    def "Map builds"() {
        given:
        ComposeFileModel x = new ComposeFileModel(new Yaml().load("""
        services:
              logstash:
                build: ./logstash
              kibana:
                build: 
                    context: ./kibana/xxx
              elastic:
                build: 
                    context: ./elastic
                    dockerfile: xx
                    args:
                        a:1
                        b:2
        """))

        when:
        x.mapBuilds { service, params ->
            "${params.context}|${params.dockerfile ?: ''}|${params.args}"
        }

        then:
        !x.parsedYml.services.logstash.containsKey('build')
        !x.parsedYml.services.kibana.containsKey('build')
        !x.parsedYml.services.elastic.containsKey('build')

        x.parsedYml.services.logstash.image == './logstash|Dockerfile|[:]'
        x.parsedYml.services.kibana.image == './kibana/xxx|Dockerfile|[:]'
        x.parsedYml.services.elastic.image == './elastic|xx|a:1 b:2'


    }
}