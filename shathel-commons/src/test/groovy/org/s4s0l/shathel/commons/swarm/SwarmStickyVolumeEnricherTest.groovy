package org.s4s0l.shathel.commons.swarm

import org.mockito.ArgumentCaptor
import org.mockito.Mockito
import org.s4s0l.shathel.commons.core.environment.EnricherExecutableParams
import org.s4s0l.shathel.commons.core.environment.ExecutableApiFacade
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


    def "Multiple volumes in a single service"() {
        given:
        SwarmStickyVolumeEnricher enricher = new SwarmStickyVolumeEnricher()
        def contextMock = getContextMock([:], """
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
        """)

        when:
        enricher.execute(contextMock)
        then:
        contextMock.compose.yml.services.spe.deploy.placement.constraints[0] == "node.labels.org.shathel.volume.deployName_db == true"
        contextMock.compose.yml.services.spe.deploy.placement.constraints[1] == "node.labels.org.shathel.volume.deployName_logs == true"
        contextMock.provisioners.size() == 2

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


    def defaultModel = """
        version: "3.1"
        services:
          service:
            image: tutum/hello-world
            volumes:
                  - volume1-data:/data
        volumes:
          volume1-data:
    """

    def getContextMock(Map<String, String> nodeLabels = [:], String ymlModel = defaultModel) {
        ExecutableApiFacade apiMock = Mockito.mock(ExecutableApiFacade)
        Mockito.when(apiMock.getNodes()).thenReturn([MockUtils.shathelManagerNode(1)])
        Mockito.when(apiMock.getNodeLabels(Mockito.any())).thenReturn(nodeLabels)
        StackDescription stackMock = Mockito.mock(StackDescription)
        Mockito.when(stackMock.getDeployName()).thenReturn("deployName")

        def provisioners = new EnricherExecutableParams.Provisioners(null, null, null)

        def model = ComposeFileModel.load(ymlModel)
        [
                api         : apiMock,
                compose     : model,
                stack       : stackMock,
                provisioners: provisioners,
                withOptional: true
        ]
    }
}
