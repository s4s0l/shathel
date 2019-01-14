package org.s4s0l.shathel.commons.swarm

import org.s4s0l.shathel.commons.core.environment.EnricherExecutable
import org.s4s0l.shathel.commons.core.environment.EnricherExecutableParams
import org.s4s0l.shathel.commons.core.environment.ProvisionerExecutable
import org.s4s0l.shathel.commons.core.environment.ProvisionerExecutableParams
import org.s4s0l.shathel.commons.core.environment.ShathelNode
import org.s4s0l.shathel.commons.core.model.ComposeFileModel
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import static org.slf4j.LoggerFactory.getLogger

/**
 * If:
 *  * there are multiple nodes (? maybe better always?)
 *  * there is a volume without label {@link #LABEL_SHATHEL_STACK_UNMANAGED} equal to true
 *  * this volume is with driver local or no driver specified
 *  * this volume is attached to service with mode replicated (or no mode) with replication=1 (or none specified)
 *      * if there is multiple services with this volume attached and one of them
 *          has mode=global or replication != 1 then exception is thrown
 * Given:
 *  * Label will have format org.shathel.volume.[serviceDeployName]_[volumeName]=true
 *
 * Then:
 *  * Node is searched by label if exist only one is used, otherwise:
 *  * If multiple exists -> exception
 *  * If all above are met then a node label is added to node:
 *      * random manager node if in any services attached to it has constraint role=manager
 *      * random node otherwise
 *  * All services get constraint with that node name
 *
 * @author Marcin Wielgus
 */
class SwarmStickyVolumeEnricher extends EnricherExecutable {
    public static
    final String LABEL_SHATHEL_STACK_UNMANAGED = "org.shathel.volume.unmanaged"
    private static final Logger LOGGER = getLogger(SwarmStickyVolumeEnricher.class);


    @Override
    protected void execute(EnricherExecutableParams params) {
        def nodes = params.apiFacade.nodes
        def model = params.model

        def volumes = new SwarmStickyVolumeFinder().searchForApplicableVolumes(model)
        if (!volumes.isEmpty()) {
            Map<ShathelNode, Map<String, String>> nodeLabels = nodes.collectEntries {
                [(it): params.apiFacade.getNodeLabels(it)]
            }
            volumes.each {
                def labelForVolume = "org.shathel.volume.${params.stack.deployName}_${it.volumeName}"
                def applicableNodes = new SwarmStickyServiceInfoFinder().getNodesApplicableFor(it.attachedServices, nodeLabels, model)
                def alreadyLabeledNodes = nodeLabels.findAll { x -> applicableNodes.contains(x.key) }
                        .findAll { x -> x.value[labelForVolume] == "true" }.collect { x -> x.key }
                if (alreadyLabeledNodes.size() > 1) {
                    throw new RuntimeException("Multiple nodes have label ${labelForVolume}!")
                }
                if (alreadyLabeledNodes.size() == 0) {
                    if(applicableNodes.size() == 0){
                        throw new RuntimeException("stack ${params.stack.deployName} cannot be run at any node, so unable to auto label some node, to make volume not floatable")
                    }
                    params.provisioners.add("sticky-volume-node-label:${labelForVolume}", getLabelingProvisioner(applicableNodes, labelForVolume))
                    //we add this label here manually, otherwise multiple volumes would
                    //exclude each other as labels required by tehem are not present at this moment

                    applicableNodes.each { xx ->
                        nodeLabels[xx][labelForVolume] =  "true"
                    }
                }
                it.attachedServices.each { s ->
                    model.addConstraintToService(s, "node.labels.${labelForVolume} == true")
                }
            }
        }
    }

    ProvisionerExecutable getLabelingProvisioner(List<ShathelNode> applicableNodes, String label) {
        { ProvisionerExecutableParams provisionerParams ->
            //we label node that has least volumes sticked
            Map<ShathelNode, Integer> nodeUsage = applicableNodes.collectEntries {
                [(it): provisionerParams.api.getNodeLabels(it).findAll {
                    it.key.startsWith("org.shathel.volume.")
                }.size()]
            }
            def nodeName = nodeUsage.find {
                it.value == nodeUsage.collect { it.value }.max()
            }.collect { it.key.nodeName }
            provisionerParams.api.managerNodeWrapper.swarmNodeSetLabels(nodeName.head(), [(label): "true"])
        } as ProvisionerExecutable
    }
}

class Constraint {
    String criterion
    boolean matches
    String what
    String constraint

    Constraint(String constraint) {
        if (constraint.contains("==")) {
            def split = constraint.split("==")
            criterion = split[0].trim()
            what = split[1].trim()
            matches = true
        } else if (constraint.contains("!=")) {
            def split = constraint.split("!=")
            criterion = split[0].trim()
            what = split[1].trim()
            matches = false
        } else {
            throw new RuntimeException("Unsupported constarint ${constraint}")
        }
        this.constraint = constraint
    }
}

class SwarmStickyServiceInfoFinder {
    private static
    final Logger LOGGER = LoggerFactory.getLogger(SwarmStickyServiceInfoFinder.class);

    List<ShathelNode> getNodesApplicableFor(List<String> serviceName, Map<ShathelNode, Map<String, String>> nodesWithLabels, ComposeFileModel model) {
        def constraints = model.yml.services.findAll {
            serviceName.contains(it.key)
        }.collect {
            it.value?.deploy?.placement?.constraints ?: []
        }.flatten().unique().collect { new Constraint(it) }
        nodesWithLabels.findAll {
            isNodeFulfillingConstraints(constraints, it.key, it.value)
        }.collect { it.key }
    }

    private boolean isNodeFulfillingConstraints(List<Constraint> constraints, ShathelNode node, Map<String, String> nodeLabels) {
        def unsatisfiedConstraints = constraints.findAll {
            boolean satisfied = true
            if (it.criterion == "node.role") {
                satisfied = (it.matches && it.what == node.role) || (!it.matches && it.what != node.role)
            } else if (it.criterion.startsWith("node.labels.")) {
                String labelName = it.criterion.substring("node.labels.".length())
                String nodeLabelValue = nodeLabels[labelName]
                satisfied = (it.matches && it.what == nodeLabelValue) || (!it.matches && it.what != nodeLabelValue)
            } else {
                LOGGER.warn("Not yet supported constraint ignored:${it.constraint}")
            }
            return !satisfied
        }
        return unsatisfiedConstraints.isEmpty()
    }

}


class SwarmStickyVolumeFinder {


    List<VolumeFound> searchForApplicableVolumes(ComposeFileModel model) {
        return model.yml.volumes
                .findAll { isVolumeInInterest(it.value) }
                .collect {
            new VolumeFound(volumeName: it.key, attachedServices: getServicesWithVolume(model, it.key))
        }
        .findAll { !it.attachedServices.isEmpty() }
    }

    private List getServicesWithVolume(ComposeFileModel model,String name) {
        def servicesWithVolumes = model.yml.services
                .findAll {
            it.value?.volumes?.find { isServiceVolumeMatching(name, it) } != null
        }
        //all services must be replicated and replication = 0
        def offendingServices = servicesWithVolumes.findAll {
            def mode = it.value?.deploy?.mode ?: "replicated"
            def replicas = it.value?.deploy?.replicas ?: 1
            mode != "replicated" || replicas != 1
        }
        //all services do not meet constraints so we asume this volume
        //is out of our interest
        if (offendingServices.size() == servicesWithVolumes.size()) {
            return []
        }
        if (offendingServices.size() != 0) {
            throw new RuntimeException("Inconsistent services for sticky volume $name (${offendingServices.collect { it.key }.join(",")})")
        }
        return servicesWithVolumes.collect { it.key }

    }

    private boolean isServiceVolumeMatching(String volumeName, volume) {
        if (volume instanceof String) {
            int colonIndex = volume.indexOf(':')
            return colonIndex == -1 ? false : volume.substring(0, colonIndex) == volumeName
        }
        volume.source == volumeName
    }

    private boolean isVolumeInInterest(volumeAttrs) {
        def driver = volumeAttrs?.driver ?: "local"
        def unmanaged = (volumeAttrs?.labels ?: [:])[SwarmStickyVolumeEnricher.LABEL_SHATHEL_STACK_UNMANAGED] ?: "false"
        return driver == "local" && unmanaged.toString() != "true"
    }


}


class VolumeFound {
    String volumeName
    List<String> attachedServices

    boolean equals(o) {
        if (this.is(o)) return true
        if (getClass() != o.class) return false

        VolumeFound that = (VolumeFound) o

        if (attachedServices != that.attachedServices) return false
        if (volumeName != that.volumeName) return false

        return true
    }

    int hashCode() {
        int result
        result = volumeName.hashCode()
        result = 31 * result + attachedServices.hashCode()
        return result
    }
}