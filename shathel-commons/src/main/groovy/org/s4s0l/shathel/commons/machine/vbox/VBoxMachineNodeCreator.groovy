package org.s4s0l.shathel.commons.machine.vbox

import org.s4s0l.shathel.commons.core.environment.EnvironmentContext
import org.s4s0l.shathel.commons.docker.DockerMachineWrapper
import org.s4s0l.shathel.commons.swarm.SwarmNodeCreator
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * @author Marcin Wielgus
 */
@Deprecated
class VBoxMachineNodeCreator implements SwarmNodeCreator{

    private static final Logger LOGGER = LoggerFactory.getLogger(VBoxMachineNodeCreator.class)
    private final DockerMachineWrapper wrapper
    private final EnvironmentContext environmentContext

    VBoxMachineNodeCreator(DockerMachineWrapper wrapper, EnvironmentContext environmentContext) {
        this.wrapper = wrapper
        this.environmentContext = environmentContext
    }


    String getMachineOpts(NetworkSettings ns) {
        "-d virtualbox --virtualbox-hostonly-cidr=${ns.getCidr(254)} --virtualbox-disk-size=20000 " +
                "--virtualbox-boot2docker-url https://github.com/boot2docker/boot2docker/releases/download/v1.13.1/boot2docker.iso"
    }

/**
 * Sets machine ip, if already static does nothing and returns it
 * @param machineName
 * @param ipNum
 * @return
 */
    SwarmNodeCreator.CreationResult staticIp( File tmpDir, String machineName, NetworkSettings ns, int ipNum) {
        boolean modified = false
        wrapper.sudo(machineName, "touch /var/lib/boot2docker/bootsync.sh")
        String sudo = wrapper.sudo(machineName, "cat /var/lib/boot2docker/bootsync.sh")
        if (sudo.contains("#SHATHELIP_START")) {
            return new SwarmNodeCreator.CreationResult((sudo =~ /#IP=([0-9\.]+)#/)[0][1], false)
        }
        def address = ns.getAddress(ipNum)
        LOGGER.info "Fixing ip address for ${machineName} to be ${address}"
        String fixationCommand = """
        #SHATHELIP_START
        #IP=${address}#
        kill `more /var/run/udhcpc.eth1.pid` && 
        ifconfig eth1 ${address} netmask ${ns.getMask()} broadcast ${ns.getBcast()} up
        #SHATELIP_END
        """
        def file = new File(tmpDir, "fix")
        try {
            file.text = fixationCommand
            wrapper.copy(file.absolutePath, "$machineName:/tmp/fixation")
            wrapper.sudo(machineName, "cp -f /tmp/fixation /var/lib/boot2docker/bootsync.sh")
            wrapper.restart(machineName)
            wrapper.regenerateCerts(machineName)
            modified = true
            wrapper.restart(machineName)
        } finally {
            file.delete()
        }

        return new SwarmNodeCreator.CreationResult(address, modified)
    }



    @Override
    SwarmNodeCreator.CreationResult createNodeIfNotExists(String machineName, NetworkSettings ns, int expectedIp, String registryMirrorHost) {
        boolean modified = false
        if (wrapper.getMachines()[machineName] == null) {
            String MACHINE_OPTS = getMachineOpts(ns)
            wrapper.create("${MACHINE_OPTS} --engine-registry-mirror ${registryMirrorHost} $machineName")
            modified = true
        }
        def ip = staticIp( environmentContext.getTempDirectory(), machineName, ns, expectedIp)
        return new SwarmNodeCreator.CreationResult(ip.ip, modified || ip.modified)
    }
}
