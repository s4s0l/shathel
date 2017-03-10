package org.s4s0l.shathel.commons.swarm;

import org.s4s0l.shathel.commons.machine.vbox.NetworkSettings;

/**
 * @author Marcin Wielgus
 */
public interface SwarmNodeCreator {
    /**
     * @param machineName        name of machine
     * @param ns                 network setting s to use for ip generation
     * @param expectedIp         number to pass to ns to get ip address
     * @param registryMirrorHost url to registry mirror to set in engine daemon
     * @return ip of machine created
     */
    CreationResult createNodeIfNotExists(String machineName, NetworkSettings ns, int expectedIp, String registryMirrorHost);


    class CreationResult {
        private final String ip;
        private final boolean modified;

        public CreationResult(String ip, boolean modified) {
            this.ip = ip;
            this.modified = modified;
        }

        public String getIp() {
            return ip;
        }

        public boolean isModified() {
            return modified;
        }
    }
}
