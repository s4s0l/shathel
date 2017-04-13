package org.s4s0l.shathel.commons.machine.vbox

/**
 * @author Marcin Wielgus
 */
@Deprecated
class NetworkSettings {
    private final String network;

    NetworkSettings(String network) {
        this.network = network
    }

    String addMissing(String addr, int val, int upto = 3) {
        String tmp = addr;
        while (tmp.count(".") < upto) {
            tmp = tmp + ".$val"
        }
        tmp
    }

    String getMask() {
        addMissing(network.replaceAll("[^\\.]+", "255"), 0);
    }

    String getBcast() {
        addMissing(network, 255, 3);
    }

    String getAddress(int val) {
        addMissing(network, 0, 2) + ".$val"
    }

    String getCidr(int val) {
        getAddress(val) + "/${8 * (network.count(".") + 1)}"
    }

}
