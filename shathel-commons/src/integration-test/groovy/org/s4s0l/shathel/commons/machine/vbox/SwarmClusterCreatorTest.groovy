package org.s4s0l.shathel.commons.machine.vbox

import org.apache.commons.io.FileUtils
import org.s4s0l.shathel.commons.core.environment.EnvironmentContext
import org.s4s0l.shathel.commons.swarm.SwarmClusterCreator
import org.s4s0l.shathel.commons.docker.DockerMachineWrapper
import org.s4s0l.shathel.commons.machine.MachineSwarmClusterWrapper
import spock.lang.IgnoreIf
import spock.lang.Specification

/**
 * @author Matcin Wielgus
 */
class SwarmClusterCreatorTest extends Specification {
    def setupSpec() {
        new DockerMachineWrapper(new File(getRootDir(),"settings")).with {
            getMachinesByName("$clusterName-.*").each { remove(it) }
        }
        FileUtils.deleteDirectory(new File(getRootDir()))
    }

    boolean cleanOnEnd() {
        setupSpec()
        true
    }


    @IgnoreIf({ Boolean.valueOf(env['IGNORE_MACHINE_TESTS']) })
    def "Schould create and destroy machine swarm cluster"() {
        given:
        SwarmClusterCreator c = new SwarmClusterCreator(
                new MachineSwarmClusterWrapper(new EnvironmentContext(null, null, null, new File(getRootDir())), new VBoxMachineSwarmClusterFlavour()),
                new File(getRootDir()),
                "$clusterName", 2, 1, "20.20.20")
        when:
        c.createMachines()
        then:
        new DockerMachineWrapper(new File(getRootDir(),"settings")).getMachinesByName("$clusterName-.*").sort() ==
                ["$clusterName-manager-1", "$clusterName-manager-2",
                 "$clusterName-worker-1"]


        cleanOnEnd()
    }

    private String getRootDir() {
        "build/Test${getClass().getSimpleName()}"
    }

    private String getClusterName() {
        return getClass().getSimpleName()
    }
}
