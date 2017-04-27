package org.s4s0l.shathel.commons.swarm

import org.s4s0l.shathel.commons.Shathel
import org.s4s0l.shathel.commons.core.DefaultGlobalEnricherProvider
import org.s4s0l.shathel.commons.core.StackOperations
import org.s4s0l.shathel.commons.core.stack.StackReference
import org.s4s0l.shathel.commons.docker.DockerWrapper
import spock.lang.Specification
import testutils.BaseIntegrationTest

/**
 * @author Marcin Wielgus
 */
class VolumeManagerTest extends BaseIntegrationTest {

    @Override
    def setupEnvironment() {
        environmentName = "local"
    }


    def cleanupEnvironment() {
        new DockerWrapper().with {
            if (swarmActive()) {
                stackUnDeploy(new File("."), "volume1")
                stackUnDeploy(new File("."), "volume2")
            }
            volumesList("label=${DefaultGlobalEnricherProvider.LABEL_SHATHEL_STACK_MARKER}=true")
                    .each { v -> volumeRemove(v.name) }
        }
    }

    def "Volumes can be deleted"() {
        given:
        Shathel sht = shathel()
        def solution = sht.getSolution(sht.initStorage(getRootDir(), false))
        def environment = solution.getEnvironment(environmentName)
        if (!environment.isInitialized()) {
            environment.initialize()
        }
        VolumeManager volumeManager = new VolumeManager(environment.environmentApiFacade)
        def volume1Reference = new StackReference("org.s4s0l.shathel:volume1:1.0")
        def volume2Reference = new StackReference("org.s4s0l.shathel:volume2:1.0")

        when:
        def stack = solution.openStack(environment, volume1Reference)
        def command = stack.createStartCommand(false)
        solution.run(command)

        stack = solution.openStack(environment, volume2Reference)
        command = stack.createStartCommand(false)
        solution.run(command)

        then:
        volumeManager.getShathelVolumes(volume1Reference).size() == 1
        volumeManager.getShathelVolumes(volume1Reference)[0].name == "volume1_volume1-data"
        volumeManager.getShathelVolumes(volume1Reference)[0].driver == "local"
        volumeManager.getShathelVolumes(volume1Reference)[0].labels[DefaultGlobalEnricherProvider.LABEL_SHATHEL_STACK_GA] == volume1Reference.getGa()
        volumeManager.getShathelVolumes(volume1Reference)[0].labels[DefaultGlobalEnricherProvider.LABEL_SHATHEL_STACK_GAV] == volume1Reference.getGav()
        volumeManager.getShathelVolumes(volume1Reference)[0].labels[DefaultGlobalEnricherProvider.LABEL_SHATHEL_STACK_MARKER] == "true"
        volumeManager.getShathelVolumes(volume2Reference).size() == 2

        volumeManager.getAllShathelVolumes(environment.environmentApiFacade.managerNode).size() == 3
        volumeManager.getShathelVolumes(environment.environmentApiFacade.managerNode, volume2Reference).size() == 2
        volumeManager.getAllShathelVolumes().size() == 3

        when:
        def purge = solution.getPurgeCommand(environment)
        solution.run(purge)

        then:
        volumeManager.getAllShathelVolumes().size() == 3

        when:
        volumeManager.deleteAllVolumes(volume1Reference)

        then:
        volumeManager.getAllShathelVolumes().size() == 2

        when:
        volumeManager.deleteVolume(volumeManager.getAllShathelVolumes()[0])

        then:
        volumeManager.getAllShathelVolumes().size() == 1

        when:
        volumeManager.deleteAllVolumes(environment.environmentApiFacade.managerNode)

        then:
        volumeManager.getAllShathelVolumes().size() == 0

        onEnd()
    }
}