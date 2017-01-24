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
        new DockerMachineWrapper(new File(getRootDir())).with{
            getMachinesByName("$clusterName-.*").each { remove(it) }
        }
    }

    boolean cleanOnEnd(){
        setupSpec()
        true
    }


    @IgnoreIf({ Boolean.valueOf(env['IGNORE_MACHINE_TESTS']) })
    def "Schould create and destroy machine swarm cluster"() {
        given:
        VBoxSwarmCluster c = new VBoxSwarmCluster(new File(getRootDir()),
                "$clusterName", 2, 1, "20.20.20")
        when:
        c.createMachines()
        then:
        new DockerMachineWrapper(new File(getRootDir())).getMachinesByName("$clusterName-.*").sort() ==
                ["$clusterName-manager-1", "$clusterName-manager-2",
                 "$clusterName-worker-1"]


        cleanOnEnd()
    }

    private String getRootDir() {
        "build/Test${getClass().getSimpleName()}"
    }

    private String getClusterName(){
        return getClass().getSimpleName()
    }
}
