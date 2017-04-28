package org.s4s0l.shathel.commons.swarm

import org.mockito.ArgumentCaptor
import org.mockito.Mockito
import org.s4s0l.shathel.commons.core.environment.EnricherExecutableParams
import org.s4s0l.shathel.commons.core.environment.ExecutableApiFacade
import org.s4s0l.shathel.commons.core.environment.ProvisionerExecutable
import org.s4s0l.shathel.commons.core.environment.ProvisionerExecutableParams
import org.s4s0l.shathel.commons.core.model.ComposeFileModel
import org.s4s0l.shathel.commons.core.stack.StackDescription
import org.s4s0l.shathel.commons.docker.DockerWrapper
import spock.lang.Specification
import testutils.MockUtils

/**
 * @author Marcin Wielgus
 */
class SwarmStickyVolumeEnricherTest extends Specification {

    def "Labeling provisioner should label a node"() {
        given:
        SwarmStickyVolumeEnricher enricher = new SwarmStickyVolumeEnricher()

        DockerWrapper wrapper = Mockito.mock(DockerWrapper)
        Mockito.doNothing().when(wrapper).swarmNodeSetLabels(Mockito.any(), Mockito.any())


        ExecutableApiFacade apiMock = Mockito.mock(ExecutableApiFacade)
        Mockito.when(apiMock.getManagerNodeWrapper()).thenReturn(wrapper)
        Mockito.when(apiMock.getNodeLabels(Mockito.any())).thenReturn([:])

        def context = [
                api: apiMock,
        ]
        def provisioner = enricher.getLabelingProvisioner([MockUtils.shathelManagerNode(1)], "org.shathel.volume.deployName_volume1-data")

        when:
        provisioner.execute(new ProvisionerExecutableParams(context))
        ArgumentCaptor<String> nodeName = ArgumentCaptor.forClass(String)
        ArgumentCaptor<Map> labels = ArgumentCaptor.forClass(Map)
        Mockito.verify(wrapper).swarmNodeSetLabels(nodeName.capture(), labels.capture())


        then:
        nodeName.value == "manager-1"
        labels.value == ["org.shathel.volume.deployName_volume1-data": "true"]

    }

    def "Should add constraint and leave provisioner for labeling node when no node has labels"() {
        given:
        SwarmStickyVolumeEnricher enricher = new SwarmStickyVolumeEnricher()
        def contextMock = getContextMock()

        when:
        enricher.execute(contextMock)
        then:
        contextMock.compose.yml.services.service.deploy.placement.constraints[0] == "node.labels.org.shathel.volume.deployName_volume1-data == true"
        contextMock.provisioners.size() == 1

    }

    def "Should add constraint and do not leave provisioner for labeling node when node already have label"() {
        given:
        SwarmStickyVolumeEnricher enricher = new SwarmStickyVolumeEnricher()
        def contextMock = getContextMock(["org.shathel.volume.deployName_volume1-data": "true"])

        when:
        enricher.execute(contextMock)
        then:
        contextMock.compose.yml.services.service.deploy.placement.constraints[0] == "node.labels.org.shathel.volume.deployName_volume1-data == true"
        contextMock.provisioners.size() == 0

    }


    def getContextMock(Map<String, String> nodeLabels = [:]) {
        ExecutableApiFacade apiMock = Mockito.mock(ExecutableApiFacade)
        Mockito.when(apiMock.getNodes()).thenReturn([MockUtils.shathelManagerNode(1)])
        Mockito.when(apiMock.getNodeLabels(Mockito.any())).thenReturn(nodeLabels)

        StackDescription stackMock = Mockito.mock(StackDescription)
        Mockito.when(stackMock.getDeployName()).thenReturn("deployName")

        def provisioners = new EnricherExecutableParams.Provisioners()

        def model = ComposeFileModel.load("""
        version: "3.1"
        services:
          service:
            image: tutum/hello-world
            volumes:
                  - volume1-data:/data
        volumes:
          volume1-data:
""")
        [
                api         : apiMock,
                compose     : model,
                stack       : stackMock,
                provisioners: provisioners,
                withOptional: true
        ]
    }
}
