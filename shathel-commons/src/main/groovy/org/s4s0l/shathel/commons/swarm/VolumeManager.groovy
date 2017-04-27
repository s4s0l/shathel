package org.s4s0l.shathel.commons.swarm

import org.s4s0l.shathel.commons.core.DefaultGlobalEnricherProvider
import org.s4s0l.shathel.commons.core.environment.ExecutableApiFacade
import org.s4s0l.shathel.commons.core.environment.ShathelNode
import org.s4s0l.shathel.commons.core.stack.StackReference
import org.s4s0l.shathel.commons.docker.DockerWrapper

/**
 * @author Marcin Wielgus
 */
class VolumeManager {
    private final ExecutableApiFacade api

    VolumeManager(ExecutableApiFacade api) {
        this.api = api
    }

    List<ShathelVolume> getAllShathelVolumes(ShathelNode node) {
        DockerWrapper docker = api.getDocker(node)
        def volumes = docker.volumesList("label=${DefaultGlobalEnricherProvider.LABEL_SHATHEL_STACK_MARKER}=true")
        return volumes.collect {
            new ShathelVolume(it.name, it.driver, node, it.labels)
        }
    }

    List<ShathelVolume> getShathelVolumes(ShathelNode node, StackReference ref) {
        return getAllShathelVolumes(node).findAll {
            it.labels[DefaultGlobalEnricherProvider.LABEL_SHATHEL_STACK_GA] == ref.getGa()
        }
    }

    List<ShathelVolume> getAllShathelVolumes() {
        return api.nodes.collect { getAllShathelVolumes(it) }.flatten()
    }

    List<ShathelVolume> getShathelVolumes(StackReference ref) {
        return api.nodes.collect { getShathelVolumes(it, ref) }.flatten()
    }

    void deleteVolume(ShathelVolume volume) {
        DockerWrapper docker = api.getDocker(volume.node)
        docker.volumeRemove(volume.name)
    }

    void deleteAllVolumes(ShathelNode node) {
        getAllShathelVolumes(node).each {
            deleteVolume(it)
        }
    }

    void deleteAllVolumes(StackReference reference) {
        getShathelVolumes(reference).each {
            deleteVolume(it)
        }
    }

}

class ShathelVolume {
    final String name
    final String driver
    final ShathelNode node
    final Map<String, String> labels

    ShathelVolume(String name, String driver, ShathelNode node, Map<String, String> labels) {
        this.name = name
        this.driver = driver
        this.node = node
        this.labels = labels
    }
}
