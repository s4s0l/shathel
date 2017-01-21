package org.s4s0l.shathel.commons.machine.vbox

import org.s4s0l.shathel.commons.docker.DockerMachineWrapper
import org.s4s0l.shathel.commons.machine.vbox.VBoxSwarmCluster
import spock.lang.*
import spock.lang.Specification

/**
 * @author Matcin Wielgus
 */
class VBoxSwarmClusterTest extends Specification {
    def setupSpec() {
       cleanupSpec();
    }

    def cleanupSpec() {
        new DockerMachineWrapper().with{
            getMachinesByName("unitTestCluster-.*").each { remove(it) }
        }
    }

    @IgnoreIf({ Boolean.valueOf(env['IGNORE_MACHINE_TESTS']) })
    def "Schould create and destroy machine swarm cluster"(){
        given:
        VBoxSwarmCluster c = new VBoxSwarmCluster(new File("build/machineWrk"),
        "unitTestCluster", 2,1,"20.20.20")
        when:
        c.create()
        then:
        new DockerMachineWrapper().getMachinesByName("unitTestCluster-.*").sort() ==
                ["unitTestCluster-manager-1","unitTestCluster-manager-2",
                 "unitTestCluster-worker-1"]

    }
}
