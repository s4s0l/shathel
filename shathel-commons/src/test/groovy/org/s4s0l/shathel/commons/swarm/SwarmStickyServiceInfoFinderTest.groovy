package org.s4s0l.shathel.commons.swarm

import org.s4s0l.shathel.commons.core.model.ComposeFileModel
import spock.lang.Specification
import spock.lang.Unroll
import testutils.MockUtils

/**
 * @author Marcin Wielgus
 */
class SwarmStickyServiceInfoFinderTest extends Specification {

    @Unroll
    def "should pick right nodes for servicesX: #desc"(String desc,
                                                      String composeModel,
                                                       List<String> serviceNames,
                                                      List<String> expectedNodeNames) {
        when:
        def nodesWithLabels = [
                (MockUtils.shathelManagerNode(1)): [
                        "org.shathel.volume.cockroach_sqlpad-data": "true",
                        "org.shathel.volume.letsencrypt_letsencrypt-data": "true",
                        "org.shathel.volume.portainer_portainer-data": "true",
                        "org.shathel.volume.registry_shathel-docker-registry-data": "true"

                ],
        ]
        def applicableFor = new SwarmStickyServiceInfoFinder().getNodesApplicableFor(serviceNames, nodesWithLabels, ComposeFileModel.load(composeModel))

        then:
        applicableFor.nodeName as Set == expectedNodeNames as Set


        where:
        desc             | composeModel   | serviceNames                                                 | expectedNodeNames
        "No constraints" | """
        version: '3.4'
        services:
          spe:
            image: project(:)
            volumes:
             - db:/spe/db/    
             - logs:/spe/log/
            ports:
              - 8600:8600
            deploy:
              endpoint_mode: vip
              mode: replicated
              replicas: 1      
              restart_policy:
                condition: on-failure
                delay: 10s
                max_attempts: 2
                window: 240s
        volumes:
          db:
          logs:

        """ | ['spe'] | ['manager-1']
    }


        @Unroll
    def "should pick right nodes for services: #desc"(String desc,
                                                      String composeModel, List<String> serviceNames,
                                                      List<String> expectedNodeNames) {
        when:
        def nodesWithLabels = [
                (MockUtils.shathelManagerNode(1)): [:],
                (MockUtils.shathelManagerNode(2)): ["set.two": "two"],
                (MockUtils.shathelManagerNode(3)): ["set.one": "one"],
                (MockUtils.shathelWorkerNode(1))  : ["set.two": "two"],
                (MockUtils.shathelWorkerNode(2))  : [
                        "set.one": "one",
                        "set.two": "two"
                ]
        ]
        def applicableFor = new SwarmStickyServiceInfoFinder().getNodesApplicableFor(serviceNames, nodesWithLabels, ComposeFileModel.load(composeModel))

        then:
        applicableFor.nodeName as Set == expectedNodeNames as Set


        where:
        desc                                        | composeModel   | serviceNames                                                 | expectedNodeNames
        "No constraints"                            | """
        version: "3.1"
        services:
          service1:
          service2:
          service3:
              deploy:
                  placement:
                    constraints:
          service4:
              deploy:
                  placement:
          service5:
              deploy:
        """ | ['service1', 'service2', 'service3', 'service4', 'service5'] | ['manager-1', 'manager-2', 'manager-3', 'worker-1', 'worker-2']


        "Manager role"                              | """
        version: "3.1"
        services:
            service1:
            service2:
              deploy:
                  placement:
                    constraints:
                        - node.role == manager
        """ | ['service1', 'service2']                                     | ['manager-1', 'manager-2', 'manager-3']

        "Not Manager role"                          | """
        version: "3.1"
        services:
            service1:
            service2:
              deploy:
                  placement:
                    constraints:
                        - node.role != manager
        """ | ['service1', 'service2']                                     | ['worker-1', 'worker-2']

        "One label"                                 | """
        version: "3.1"
        services:
            service1:
            service2:
              deploy:
                  placement:
                    constraints:
                        - node.labels.set.one ==   one
        """ | ['service1', 'service2']                                     | ['worker-2', 'manager-3']

        "Not Label"                                 | """
        version: "3.1"
        services:
            service1:
            service2:
              deploy:
                  placement:
                    constraints:
                        - node.labels.set.one !=   one
        """ | ['service1', 'service2']                                     | ['worker-1', 'manager-1', 'manager-2']

        "Two Labels in one service"                 | """
        version: "3.1"
        services:
            service1:
            service2:
              deploy:
                  placement:
                    constraints:
                        - node.labels.set.one ==   one
                        - node.labels.set.two ==   two
        """ | ['service1', 'service2']                                     | ['worker-2']

        "Two Labels in different services"          | """
        version: "3.1"
        services:
            service1:
              deploy:
                  placement:
                    constraints:
                        - node.labels.set.two ==   two
            service2:
              deploy:
                  placement:
                    constraints:
                        - node.labels.set.one ==   one
        """ | ['service1', 'service2']                                     | ['worker-2']

        "Two Labels in different services and role" | """
        version: "3.1"
        services:
            service1:
              deploy:
                  placement:
                    constraints:
                        - node.role == manager
                        - node.labels.set.two !=   two
            service2:
              deploy:
                  placement:
                    constraints:
                        - node.labels.set.one ==   one
        """ | ['service1', 'service2']                                     | ['manager-3']

        "No match" | """
        version: "3.1"
        services:
            service1:
              deploy:
                  placement:
                    constraints:
                        - node.role == manager
            service2:
              deploy:
                  placement:
                    constraints:
                        - node.labels.set.two ==two                    
            service3:
              deploy:
                  placement:
                    constraints:
                        - node.labels.set.one ==   one
        """ | ['service1', 'service2','service3']                                     | []

    }

}
