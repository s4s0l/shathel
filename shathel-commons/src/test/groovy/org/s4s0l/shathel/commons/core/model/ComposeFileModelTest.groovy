package org.s4s0l.shathel.commons.core.model

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.yaml.snakeyaml.Yaml
import spock.lang.Specification

/**
 * @author Marcin Wielgus
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

    def "addLabels"() {
        given:
        ComposeFileModel x = new ComposeFileModel(new Yaml().load("""
        version: "3"
        services:
          service1:
          service2:
            labels:
                label: value
            deploy:
              labels:
                label: value        
        volumes:
          volume0:
            external:
                name: extName  
          volume1:
          volume2:
            labels:
                label: value
        networks:
            network1:
                external: true 
            network2:
            network3:
                labels:
                    label: value          
        """))

        when:
        x.addLabelToServices("sss", "sss")
        x.addLabelToNetworks("nnn", "nnn")
        x.addLabelToVolumes("vvv", "vvv")
        def dump = new Yaml().dump(x.parsedYml)
        def res = new Yaml().load(dump)


        then:
        res.services.service1.labels  == [sss:'sss']
        res.services.service1.deploy.labels  == [sss:'sss']
        res.services.service2.labels  == [label:'value',sss:'sss']
        res.services.service2.deploy.labels  == [label:'value',sss:'sss']
        res.volumes.volume1.labels == [vvv:'vvv']
        res.volumes.volume2.labels == [label:'value',vvv:'vvv']
        res.volumes.volume0.labels == null
        res.networks.network1.labels == null
        res.networks.network2.labels == [nnn:'nnn']
        res.networks.network3.labels == [label:'value',nnn:'nnn']
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


    def "Find labeled services "() {
        given:
        ComposeFileModel x = new ComposeFileModel(new Yaml().load("""
        services:
              aaaa:
                  build: ./logstash
                  deploy:
                        labels:
                            com.df.notify: "true"
                            com.df.usersSecret: "monitoring"
                            com.df.distribute: "true"
                            com.df.servicePath: "/prometheus"
                            com.df.port: "9090"
              bbbb:
                build: 
                    context: ./kibana/xxx
        """))

        when:
        def services = x.findServicesWithLabels("com.df.notify", "true")

        then:
        services.size() == 1
        services[0]['build'] == './logstash'


    }


    def "Map secrets"() {
        given:
        ComposeFileModel x = new ComposeFileModel(new Yaml().load("""
        services:
              serviceNoSecrets:
              serviceEmptySecrets:
                secrets:
              service1:
                  secrets:
                      - my_first_secret
                      - my_second_secret
                      - dummy
              service2:
                  secrets:
                    -   source: my_first_secret
                        target: targetSecret
                        uid: '103'
                        gid: '103'
                        mode: 0440
                    -   source: dummy
                        target: other
                        uid: '103'
                        gid: '103'
                        mode: 0440
                    -   my_second_secret    
        secrets:
            my_first_secret:
                file: ./secret_data
            my_second_secret:
                external: true
            dummy:
                file: ./dummy_secret_data       
        """))

        when:
        x.mapSecrets { it ->
            if (it.name == "my_second_secret") {
                return [name: "my_second_secret_2", external: true]
            }
            if (it.name == "my_first_secret") {
                return [name: "external_my_first_secret2", external: true]
            }
            it
        }

        then:
        x.yml.secrets.size() == 3
        x.yml.secrets['dummy'] == [file: './dummy_secret_data']
        x.yml.secrets['my_second_secret_2'] == [external: true]
        x.yml.secrets['external_my_first_secret2'] == [external: true]

        x.yml.services.service1.secrets == [
                [source: 'external_my_first_secret2',
                 target: 'my_first_secret'],
                [source: 'my_second_secret_2',
                 target: 'my_second_secret'],
                'dummy'

        ]

        x.yml.services.service2.secrets == [
                [source: 'external_my_first_secret2',
                 target: 'targetSecret',
                 uid   : '103',
                 gid   : '103',
                 mode  : 0440],
                [source: 'dummy',
                 target: 'other',
                 uid   : '103',
                 gid   : '103',
                 mode  : 0440],
                [source: 'my_second_secret_2',
                 target: 'my_second_secret']

        ]

    }


}
