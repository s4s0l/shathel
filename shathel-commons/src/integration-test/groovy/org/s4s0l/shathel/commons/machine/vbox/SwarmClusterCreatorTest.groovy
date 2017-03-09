package org.s4s0l.shathel.commons.machine.vbox

import org.apache.commons.io.FileUtils
import org.mockito.Mockito
import org.s4s0l.shathel.commons.core.MapParameters
import org.s4s0l.shathel.commons.core.environment.EnvironmentContext
import org.s4s0l.shathel.commons.core.environment.EnvironmentDescription
import org.s4s0l.shathel.commons.core.storage.Storage
import org.s4s0l.shathel.commons.swarm.SwarmClusterCreator
import org.s4s0l.shathel.commons.docker.DockerMachineWrapper
import org.s4s0l.shathel.commons.machine.MachineSwarmClusterWrapper
import org.s4s0l.shathel.commons.utils.ExtensionContext
import spock.lang.IgnoreIf
import spock.lang.Specification

/**
 * @author Matcin Wielgus
 */
class SwarmClusterCreatorTest extends Specification {
    def setupSpec() {
        new DockerMachineWrapper(settingsDir()).with {
            getMachinesByName("$clusterName-.*").each { remove(it) }
        }
        FileUtils.deleteDirectory(new File(getRootDir()))
    }

    private File ensureExists(File f) {
        f.mkdirs();
        return f;
    }

    private File settingsDir() {
        ensureExists(new File(getRootDir(), "settings"))
    }

    private File tempDir() {
        ensureExists(new File(getRootDir(), "temp"))
    }

    boolean cleanOnEnd() {
        setupSpec()
        true
    }


    @IgnoreIf({ Boolean.valueOf(env['IGNORE_MACHINE_TESTS']) })
    def "Schould create and destroy machine swarm cluster"() {
        given:
        Storage storage = Mockito.mock(Storage)
        Mockito.when(storage.getSettingsDirectory(Mockito.any(), Mockito.anyString())).thenReturn(settingsDir())
        Mockito.when(storage.getTemptDirectory(Mockito.any(), Mockito.anyString())).thenReturn(tempDir())

        EnvironmentDescription desc = new EnvironmentDescription(MapParameters.builder().build(), "xxx", "vbox", [:])
        def context = new EnvironmentContext(new ExtensionContext(), desc, null, null, storage);
        def wrapper = new MachineSwarmClusterWrapper(context)
        SwarmClusterCreator c = new SwarmClusterCreator(
                wrapper,
                new VBoxMachineNodeCreator(wrapper.getWrapper(), context),
                new File(getRootDir()),
                "$clusterName", 2, 1, "20.20.20")
        when:
        c.createMachines()
        then:
        new DockerMachineWrapper(new File(getRootDir(), "settings")).getMachinesByName("$clusterName-.*").sort() ==
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
