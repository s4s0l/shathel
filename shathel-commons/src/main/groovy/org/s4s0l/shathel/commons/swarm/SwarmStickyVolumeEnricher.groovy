package org.s4s0l.shathel.commons.swarm

import org.s4s0l.shathel.commons.core.environment.EnricherExecutable
import org.s4s0l.shathel.commons.core.environment.EnricherExecutableParams
import org.slf4j.Logger

import static org.slf4j.LoggerFactory.getLogger

/**
 * If:
 *  * there are multiple nodes (? maybe better always?)
 *  * there is a volume without label {@link #LABEL_SHATHEL_STACK_UNMANAGED} equal to true
 *  * this volume is with driver local or no driver specified
 *  * this volume is attached to service with mode replicated (or no mode) with replication=1 (or none specified)
 *      * if there is multiple services with this volume attached and one of them
 *          has mode=global or replication != 1 then warning is printed
 * Given:
 *  * Label will have format org.shathel.volume.[serviceDeployName]_[volumeName]=true
 *
 * Then:
 *  * Node is searched by label if exist is used otherwise:
 *  * If all above are met then a node label is added to node:
 *      * random manager node if in any services attached to it there is constraint role=manager
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

    }
}
