package org.s4s0l.shathel.commons.machine

import org.s4s0l.shathel.commons.docker.DockerMachineWrapper
import org.s4s0l.shathel.commons.machine.vbox.NetworkSettings
import org.s4s0l.shathel.commons.swarm.SwarmClusterWrapper

/**
 * @author Matcin Wielgus
 */
interface MachineSwarmClusterFlavour {
    String getMachineOpts(NetworkSettings ns)

    SwarmClusterWrapper.CreationResult staticIp(DockerMachineWrapper wrapper, File tmpDir, String machineName, NetworkSettings ns, int ipNum)

}