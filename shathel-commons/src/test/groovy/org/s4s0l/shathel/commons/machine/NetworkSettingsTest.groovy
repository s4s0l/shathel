package org.s4s0l.shathel.commons.machine

import spock.lang.Specification

/**
 * @author Matcin Wielgus
 */
class NetworkSettingsTest extends Specification {

    def "Should work for x.x.x"(){
        when:
        NetworkSettings ns = new NetworkSettings("1.2.3");

        then:
        ns.getMask() == "255.255.255.0"
        ns.getBcast() == "1.2.3.255"
        ns.getAddress(254) == "1.2.3.254"
        ns.getCidr(254) == "1.2.3.254/24"

        when:
        ns = new NetworkSettings("1.2");

        then:
        ns.getMask() == "255.255.0.0"
        ns.getBcast() == "1.2.255.255"
        ns.getAddress(254) == "1.2.0.254"
        ns.getCidr(254) == "1.2.0.254/16"
    }
}
