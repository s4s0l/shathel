package org.s4s0l.shathel.commons.swarm

import org.s4s0l.shathel.commons.core.model.ComposeFileModel
import spock.lang.Specification
import spock.lang.Unroll

/**
 * @author Marcin Wielgus
 */
class SwarmStickyVolumeFinderTest extends Specification {
    @Unroll
    def "Finder should fail if inconsistent config: #desc"(desc, compose, result) {
        when:
        new SwarmStickyVolumeFinder().searchForApplicableVolumes(ComposeFileModel.load(compose))
        then:
        thrown(result)


        where:
        desc                                    | compose        | result
        "two services inconsistent replication" | """
        version: "3.1"
        services:
          service:
            image: tutum/hello-world
            volumes:
                  - volume1-data:/data
          service1:
            image: tutum/hello-world
            volumes:
                  - volume1-data:/data
            deploy:
              mode: replicated
              replicas: 2          
        volumes:
          volume1-data:
        """ | RuntimeException

        "two services inconsistent mode"        | """
        version: "3.1"
        services:
          service:
            image: tutum/hello-world
            volumes:
                  - volume1-data:/data
          service1:
            image: tutum/hello-world
            volumes:
                  - volume1-data:/data
            deploy:
              mode: global
        volumes:
          volume1-data:
        """ | RuntimeException
    }

    @Unroll
    def "Finder should find correct volumes: #desc"(desc, compose, result) {
        when:
        def volumes = new SwarmStickyVolumeFinder().searchForApplicableVolumes(ComposeFileModel.load(compose))
        then:
        volumes == result


        where:
        desc                                        | compose        | result
        "No volumes"                                | """
        version: "3.1"
        services:
          service:
            image: tutum/hello-world
        """ | []

        "Volumes missing"                           | """
        version: "3.1"
        services:
          service:
            image: tutum/hello-world
        volumes:    
        """ | []

        "One volume one service"                    | """
        version: "3.1"
        services:
          service:
            image: tutum/hello-world
            volumes:
                  - volume1-data:/data
        volumes:
          volume1-data:
        """ | [new VolumeFound(volumeName: "volume1-data", attachedServices: ["service"])]

        "Two volumes one service"                   | """
        version: "3.1"
        services:
          service:
            image: tutum/hello-world
            volumes:
                  - volume1-data:/data
                  - volume2-data:/data
        volumes:
          volume1-data:
          volume2-data:
        """ | [new VolumeFound(volumeName: "volume1-data", attachedServices: ["service"]),
               new VolumeFound(volumeName: "volume2-data", attachedServices: ["service"])]

        "Not attached volume"                       | """
        version: "3.1"
        services:
          service:
            image: tutum/hello-world
            volumes:
                  - volume1-data:/data
        volumes:
          volume1-data:
          volume2-data:
        """ | [new VolumeFound(volumeName: "volume1-data", attachedServices: ["service"])]

        "No volumes in service 1"                   | """
        version: "3.1"
        services:
          service:
            image: tutum/hello-world
        volumes:
          volume1-data:
        """ | []

        "No volumes in service 2"                   | """
        version: "3.1"
        services:
          service:
            image: tutum/hello-world
          volumes:  
        volumes:
          volume1-data:
        """ | []

        "Local Driver volume"                       | """
        version: "3.1"
        services:
          service:
            image: tutum/hello-world
            volumes:
                  - volume1-data:/data
        volumes:
          volume1-data:
            driver: local
        """ | [new VolumeFound(volumeName: "volume1-data", attachedServices: ["service"])]


        "Other Driver volume"                       | """
        version: "3.1"
        services:
          service:
            image: tutum/hello-world
            volumes:
                  - volume1-data:/data
                  - volume2-data:/data
        volumes:
          volume2-data:
          volume1-data:
            driver: other
        """ | [new VolumeFound(volumeName: "volume2-data", attachedServices: ["service"])]

        "Unmanaged volume"                          | """
        version: "3.1"
        services:
          service:
            image: tutum/hello-world
            volumes:
                  - volume1-data:/data
                  - volume2-data:/data
        volumes:
          volume2-data:
          volume1-data:
            labels:
                org.shathel.volume.unmanaged: true
        """ | [new VolumeFound(volumeName: "volume2-data", attachedServices: ["service"])]

        "Unmanaged volume as string"                | """
        version: "3.1"
        services:
          service:
            image: tutum/hello-world
            volumes:
                  - volume1-data:/data
                  - volume2-data:/data
        volumes:
          volume2-data:
          volume1-data:
            labels:
                org.shathel.volume.unmanaged: "true"
        """ | [new VolumeFound(volumeName: "volume2-data", attachedServices: ["service"])]


        "Unmanaged volume set to other than true"   | """
        version: "3.1"
        services:
          service:
            image: tutum/hello-world
            volumes:
                  - volume1-data:/data
                  - volume2-data:/data
        volumes:
          volume1-data:
            labels:
                org.shathel.volume.unmanaged: "false"
        """ | [new VolumeFound(volumeName: "volume1-data", attachedServices: ["service"])]


        "One volume two services"                   | """
        version: "3.1"
        services:
          service1:
            image: tutum/hello-world
            volumes:
                  - volume1-data:/data
          service2:
            image: tutum/hello-world
            volumes:
                  - volume1-data:/data        
        volumes:
          volume1-data:
        """ | [new VolumeFound(volumeName: "volume1-data", attachedServices: ["service1", "service2"])]


        "One volume two services"                   | """
        version: "3.1"
        services:
          service1:
            image: tutum/hello-world
            volumes:
                  - volume1-data:/data
          service2:
            image: tutum/hello-world
            volumes:
                  - volume1-data:/data        
        volumes:
          volume1-data:
        """ | [new VolumeFound(volumeName: "volume1-data", attachedServices: ["service1", "service2"])]

        "Replicated service replicas = 1"           | """
        version: "3.1"
        services:
          service:
            image: tutum/hello-world
            volumes:
                  - volume1-data:/data
            deploy:
              mode: replicated
              replicas: 1      
        volumes:
          volume1-data:
        """ | [new VolumeFound(volumeName: "volume1-data", attachedServices: ["service"])]

        "Replicated service replicated no replicas" | """
        version: "3.1"
        services:
          service:
            image: tutum/hello-world
            volumes:
                  - volume1-data:/data
            deploy:
              mode: replicated
        volumes:
          volume1-data:
        """ | [new VolumeFound(volumeName: "volume1-data", attachedServices: ["service"])]

        "Replicated service global"                 | """
        version: "3.1"
        services:
          service:
            image: tutum/hello-world
            volumes:
                  - volume1-data:/data
            deploy:
              mode: global
        volumes:
          volume1-data:
        """ | []

        "One volume one service - long syntax"      | """
        version: "3.1"
        services:
          service:
            image: tutum/hello-world
            volumes:
              - type: volume
                source: volume1-data
                target: /data
        volumes:
          volume1-data:
        """ | [new VolumeFound(volumeName: "volume1-data", attachedServices: ["service"])]


    }
}
