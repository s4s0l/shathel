package org.s4s0l.shathel.commons.docker

import java.util.concurrent.ConcurrentHashMap

/**
 * todo:
 * This caching is bullshit as it invalidates cache only on docker machine commands but
 * caches docker state also, especially swarm info, so after joining leaving swarm
 * this cache is inconsistent:/ Maybe caching should be pushed to swarmwrapper or sth....
 * @author Marcin Wielgus
 */
class DockerMachineCachingWrapper extends DockerMachineWrapper{
    DockerMachineCachingWrapper(File storageDir) {
        super(storageDir)
    }

    DockerMachineCachingWrapper() {
    }

    @Override
    synchronized void restart(String node) {
        machinesCache = null
        super.restart(node)
    }

    @Override
    synchronized void regenerateCerts(String node) {
        machinesCache = null
        super.regenerateCerts(node)
    }

    @Override
    synchronized void create(String string) {
        machinesCache = null
        super.create(string)
    }

    @Override
    synchronized void remove(String machineName) {
        machinesCache = null
        super.remove(machineName)
    }

    @Override
    synchronized void stop(String machineName) {
        machinesCache = null
        super.stop(machineName)
    }

    @Override
    synchronized boolean start(String machineName) {
        machinesCache = null
        return super.start(machineName)
    }

    @Override
    synchronized Map<String, DockerMachineNode> getMachines() {
        if(machinesCache == null){
            machinesCache = Collections.unmodifiableMap(super.getMachines())
        }
        return machinesCache
    }

    private Map<String, DockerMachineNode> machinesCache;
}
